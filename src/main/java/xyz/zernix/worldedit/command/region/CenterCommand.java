package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.List;

/**
 * Fills geometric center of current selection.
 */
public final class CenterCommand extends RegionCommand {
    public CenterCommand() {
        super("center", "worldedit:command.center.description", List.of("middle"));
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

                        int[] xs = centers(bounds.minX(), bounds.maxX());
                        int[] ys = centers(bounds.minY(), bounds.maxY());
                        int[] zs = centers(bounds.minZ(), bounds.maxZ());
                        for (int x : xs) {
                            for (int y : ys) {
                                for (int z : zs) {
                                    snapshot.setBlockState(x, y, z, pattern.next());
                                }
                            }
                        }
                        player.sendTranslatable(RegionMessages.CENTER_SUCCESS, xs.length * ys.length * zs.length);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }

    private static int[] centers(int min, int max) {
        int size = max - min + 1;
        int low = min + (size - 1) / 2;
        if (size % 2 == 0) {
            return new int[]{low, low + 1};
        }
        return new int[]{low};
    }
}

