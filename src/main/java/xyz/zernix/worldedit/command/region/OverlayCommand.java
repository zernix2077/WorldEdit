package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Places pattern on top of terrain columns inside selection.
 */
public final class OverlayCommand extends RegionCommand {
    public OverlayCommand() {
        super("overlay", "worldedit:command.overlay.description");
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

                        var dimension = player.getLocation().dimension();
                        int total = 0;
                        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                Integer top = null;
                                for (int y = bounds.maxY(); y >= bounds.minY(); y--) {
                                    if (dimension.getBlockState(x, y, z).getBlockStateData().hasCollision()) {
                                        top = y;
                                        break;
                                    }
                                }
                                if (top == null) {
                                    continue;
                                }

                                int ty = top + 1;
                                if (ty <= bounds.maxY()
                                        && dimension.isInWorld(x, ty, z)
                                        && !dimension.getBlockState(x, ty, z).getBlockStateData().hasCollision()) {
                                    snapshot.setBlockState(x, ty, z, pattern.next());
                                    total++;
                                }
                            }
                        }
                        player.sendTranslatable(RegionMessages.OVERLAY_SUCCESS, total);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }
}

