package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.command.arg.BlockMask;
import xyz.zernix.worldedit.command.arg.BlockMaskNode;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Counts blocks inside the selection matching a block mask.
 */
public final class CountCommand extends WorldEditCommand {
    public CountCommand() {
        super("count", "worldedit:command.count.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new BlockMaskNode("mask", parent))
                .exec((context, player) -> {
                    Selection.Bounds bounds = Selection.bounds(player);
                    if (bounds == null) {
                        return context.success();
                    }

                    BlockMask mask = context.getResult(0);
                    var dimension = player.getLocation().dimension();
                    int total = 0;
                    for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                        for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                if (mask.test(dimension.getBlockState(x, y, z))) {
                                    total++;
                                }
                            }
                        }
                    }

                    player.sendTranslatable(SelectionMessages.COUNTED, total);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

