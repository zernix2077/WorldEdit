package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.world.Dimension;

/**
 * Shared navigation helpers.
 */
final class NavigationUtil {
    static final int VERTICAL_SEARCH_HEIGHT = 64;
    static final double TRACE_STEP = 0.2d;

    private NavigationUtil() {
    }

    static boolean isMovementBlocker(BlockState state) {
        return state.getBlockStateData().hasCollision();
    }

    static boolean isPlayerHarming(BlockState state) {
        var blockType = state.getBlockType();
        return isMovementBlocker(state)
                || blockType == BlockTypes.LAVA
                || blockType == BlockTypes.FIRE
                || blockType == BlockTypes.SOUL_FIRE
                || blockType == BlockTypes.CACTUS;
    }

    static boolean safeForStanding(Dimension dimension, int x, int y, int z) {
        BlockState feet = dimension.getBlockState(x, y, z);
        BlockState head = dimension.getBlockState(x, y + 1, z);
        BlockState below = dimension.getBlockState(x, y - 1, z);

        return dimension.isYInRange(y)
                && dimension.isYInRange(y + 1)
                && dimension.isYInRange(y - 1)
                && !isPlayerHarming(feet)
                && !isPlayerHarming(head)
                && isMovementBlocker(below);
    }

    static boolean teleportCentered(EntityPlayer player, int x, int y, int z) {
        var location = player.getLocation();
        return player.teleport(new Location3d(x + 0.5d, y, z + 0.5d, location.pitch(), location.yaw(), location.dimension()));
    }

    static boolean findFreePosition(EntityPlayer player, int x, int y, int z) {
        var dimension = player.getLocation().dimension();
        int originalY = Math.max(dimension.getDimensionInfo().minHeight(), y);
        int maxY = Math.min(dimension.getDimensionInfo().maxHeight(), originalY + VERTICAL_SEARCH_HEIGHT) + 2;

        int free = 0;
        for (int yy = originalY; yy <= maxY; yy++) {
            if (!isMovementBlocker(dimension.getBlockState(x, yy, z))) {
                free++;
                if (free == 2) {
                    int candidate = yy - 1;
                    if (candidate != originalY) {
                        return teleportCentered(player, x, candidate, z);
                    }
                }
            } else {
                free = 0;
            }
        }
        return false;
    }
}

