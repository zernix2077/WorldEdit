package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockMask;
import xyz.zernix.worldedit.command.arg.BlockMaskNode;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Replaces matching blocks in the selected area.
 */
public final class ReplaceCommand extends RegionCommand {
    public ReplaceCommand() {
        super("replace", "worldedit:command.replace.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode from = tree.getRoot().addLeaf(parent -> new BlockMaskNode("from", parent));
        from.addLeaf(parent -> new BlockPatternNode("to", parent))
                .exec((context, player) -> {
                    BlockMask mask = context.getResult(0);
                    BlockPattern pattern = context.getResult(1);
                    return runWithSnapshot(context, player, snapshot -> {
                        Selection.Bounds bounds = Selection.bounds(player);
                        if (bounds == null) {
                            return;
                        }

                        var dimension = player.getLocation().dimension();
                        int replaced = 0;
                        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                    if (mask.test(dimension.getBlockState(x, y, z))) {
                                        snapshot.setBlockState(x, y, z, pattern.next());
                                        replaced++;
                                    }
                                }
                            }
                        }
                        player.sendTranslatable(RegionMessages.REPLACE_SUCCESS, replaced);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }
}

