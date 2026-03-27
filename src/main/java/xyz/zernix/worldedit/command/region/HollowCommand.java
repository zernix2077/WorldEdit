package xyz.zernix.worldedit.command.region;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Makes current selection hollow with configurable shell thickness.
 */
public final class HollowCommand extends RegionCommand {
    public HollowCommand() {
        super("hollow", "worldedit:command.hollow.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("thickness")
                .optional()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .optional()
                .exec((context, player) -> {
                    int thickness = Math.max(1, context.getResult(0));
                    BlockPattern pattern = context.getResult(1);
                    return runWithSnapshot(context, player, snapshot -> {
                        Selection.Bounds bounds = Selection.bounds(player);
                        if (bounds == null) {
                            return;
                        }

                        BlockState air = BlockTypes.AIR.getDefaultState();
                        int total = 0;
                        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                    int borderDist = Math.min(
                                            Math.min(x - bounds.minX(), bounds.maxX() - x),
                                            Math.min(Math.min(y - bounds.minY(), bounds.maxY() - y), Math.min(z - bounds.minZ(), bounds.maxZ() - z))
                                    );
                                    BlockState next;
                                    if (borderDist < thickness) {
                                        next = pattern != null ? pattern.next() : air;
                                    } else {
                                        next = air;
                                    }
                                    snapshot.setBlockState(x, y, z, next);
                                    total++;
                                }
                            }
                        }
                        player.sendTranslatable(RegionMessages.HOLLOW_SUCCESS, total);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }
}

