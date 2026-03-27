package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Replaces side walls of current selection.
 */
public final class WallsCommand extends RegionCommand {
    public WallsCommand() {
        super("walls", "worldedit:command.walls.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .exec((context, player) -> {
                    BlockPattern pattern = context.getResult(0);
                    return runWithSnapshot(context, player, snapshot -> {
                        Selection.Bounds bounds = Selection.bounds(player);
                        if (bounds == null) {
                            return;
                        }

                        int total = 0;
                        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                    if (x == bounds.minX() || x == bounds.maxX() || z == bounds.minZ() || z == bounds.maxZ()) {
                                        snapshot.setBlockState(x, y, z, pattern.next());
                                        total++;
                                    }
                                }
                            }
                        }

                        player.sendTranslatable(RegionMessages.WALLS_SUCCESS, total);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }
}

