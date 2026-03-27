package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.command.arg.BlockMask;
import xyz.zernix.worldedit.command.arg.BlockMaskNode;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Trims selection to the minimal cuboid containing blocks matching the mask.
 */
public final class TrimCommand extends WorldEditCommand {
    public TrimCommand() {
        super("trim", "worldedit:command.trim.description");
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

                    int minY = bounds.maxY() + 1;
                    for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                        if (anyAtY(mask, dimension, bounds, y)) {
                            minY = y;
                            break;
                        }
                    }

                    if (minY > bounds.maxY()) {
                        player.sendTranslatable(SelectionMessages.TRIM_NO_BLOCKS);
                        return context.success();
                    }

                    int maxY = minY - 1;
                    for (int y = bounds.maxY(); y >= minY; y--) {
                        if (anyAtY(mask, dimension, bounds, y)) {
                            maxY = y;
                            break;
                        }
                    }

                    int minX = bounds.maxX() + 1;
                    for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                        if (anyAtX(mask, dimension, bounds, x, minY, maxY)) {
                            minX = x;
                            break;
                        }
                    }
                    int maxX = minX - 1;
                    for (int x = bounds.maxX(); x >= minX; x--) {
                        if (anyAtX(mask, dimension, bounds, x, minY, maxY)) {
                            maxX = x;
                            break;
                        }
                    }

                    int minZ = bounds.maxZ() + 1;
                    for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                        if (anyAtZ(mask, dimension, z, minX, maxX, minY, maxY)) {
                            minZ = z;
                            break;
                        }
                    }
                    int maxZ = minZ - 1;
                    for (int z = bounds.maxZ(); z >= minZ; z--) {
                        if (anyAtZ(mask, dimension, z, minX, maxX, minY, maxY)) {
                            maxZ = z;
                            break;
                        }
                    }

                    Selection.Bounds out = new Selection.Bounds(minX, maxX, minY, maxY, minZ, maxZ);
                    if (Selection.applyBounds(player, out)) {
                        player.sendTranslatable(SelectionMessages.TRIM_DONE);
                    } else {
                        player.sendTranslatable(SelectionMessages.TRIM_NO_BLOCKS);
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }

    private static boolean anyAtY(BlockMask mask, org.allaymc.api.world.Dimension dimension, Selection.Bounds bounds, int y) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                if (mask.test(dimension.getBlockState(x, y, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean anyAtX(BlockMask mask, org.allaymc.api.world.Dimension dimension, Selection.Bounds bounds, int x, int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                if (mask.test(dimension.getBlockState(x, y, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean anyAtZ(BlockMask mask, org.allaymc.api.world.Dimension dimension, int z, int minX, int maxX, int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (mask.test(dimension.getBlockState(x, y, z))) {
                    return true;
                }
            }
        }
        return false;
    }
}

