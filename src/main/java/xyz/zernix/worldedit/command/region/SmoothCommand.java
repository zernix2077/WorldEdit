package xyz.zernix.worldedit.command.region;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockMask;
import xyz.zernix.worldedit.command.arg.BlockMaskNode;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Smooths selected area by neighborhood majority state.
 */
public final class SmoothCommand extends RegionCommand {
    public SmoothCommand() {
        super("smooth", "worldedit:command.smooth.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode iterationsNode = tree.getRoot().intNum("iterations").optional();
        iterationsNode.exec((context, player) -> run(context, player, Math.max(1, context.getResult(0)), null), SenderType.ACTUAL_PLAYER);

        iterationsNode
                .addLeaf(parent -> new BlockMaskNode("mask", parent))
                .optional()
                .exec((context, player) -> run(context, player, Math.max(1, context.getResult(0)), context.getResult(1)), SenderType.ACTUAL_PLAYER);

        tree.getRoot()
                .addLeaf(parent -> new BlockMaskNode("mask", parent))
                .exec((context, player) -> run(context, player, 1, context.getResult(0)), SenderType.ACTUAL_PLAYER);
    }

    private org.allaymc.api.command.CommandResult run(
            org.allaymc.api.command.tree.CommandContext context,
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            int iterations,
            BlockMask mask
    ) {
        return runWithSnapshot(context, player, snapshot -> {
            Selection.Bounds bounds = Selection.bounds(player);
            if (bounds == null) {
                return;
            }

            var dimension = player.getLocation().dimension();
            int total = 0;
            for (int i = 0; i < iterations; i++) {
                List<Change> changes = new ArrayList<>();
                for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                    for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                        for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                            BlockState current = dimension.getBlockState(x, y, z);
                            if (mask != null && !mask.test(current)) {
                                continue;
                            }

                            HashMap<BlockState, Integer> counts = new HashMap<>();
                            for (int ox = -1; ox <= 1; ox++) {
                                for (int oy = -1; oy <= 1; oy++) {
                                    for (int oz = -1; oz <= 1; oz++) {
                                        int sx = x + ox;
                                        int sy = y + oy;
                                        int sz = z + oz;
                                        if (sx < bounds.minX() || sx > bounds.maxX()
                                                || sy < bounds.minY() || sy > bounds.maxY()
                                                || sz < bounds.minZ() || sz > bounds.maxZ()) {
                                            continue;
                                        }
                                        BlockState neighbor = dimension.getBlockState(sx, sy, sz);
                                        counts.merge(neighbor, 1, Integer::sum);
                                    }
                                }
                            }

                            BlockState next = current;
                            int best = -1;
                            for (var entry : counts.entrySet()) {
                                if (entry.getValue() > best) {
                                    best = entry.getValue();
                                    next = entry.getKey();
                                }
                            }

                            if (next != current) {
                                changes.add(new Change(x, y, z, next));
                            }
                        }
                    }
                }

                for (Change change : changes) {
                    snapshot.setBlockState(change.x, change.y, change.z, change.state);
                }
                total += changes.size();
            }

            player.sendTranslatable(RegionMessages.SMOOTH_SUCCESS, iterations, total);
        });
    }

    private record Change(int x, int y, int z, BlockState state) {
    }
}

