package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.NavigationMessages;

/**
 * Moves player to the nearest reachable ceiling.
 */
public final class CeilCommand extends WorldEditCommand {
    public CeilCommand() {
        super("ceil", "worldedit:command.ceil.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            var location = player.getLocation();
            var dimension = location.dimension();
            int x = (int) Math.floor(location.x());
            int z = (int) Math.floor(location.z());
            int initialY = Math.max(dimension.getDimensionInfo().minHeight(), (int) Math.floor(location.y()));
            int y = Math.max(dimension.getDimensionInfo().minHeight(), initialY + 2);

            boolean moved = false;
            if (!NavigationUtil.isMovementBlocker(dimension.getBlockState(x, y, z))) {
                int maxY = Math.min(dimension.getDimensionInfo().maxHeight(), y + NavigationUtil.VERTICAL_SEARCH_HEIGHT);
                for (int yy = y; yy <= maxY; yy++) {
                    if (NavigationUtil.isMovementBlocker(dimension.getBlockState(x, yy, z))) {
                        int platformY = Math.max(initialY, yy - 3);
                        if (platformY > initialY) {
                            moved = NavigationUtil.teleportCentered(player, x, platformY + 1, z);
                        }
                        break;
                    }
                }
            }

            if (moved) {
                player.sendTranslatable(NavigationMessages.CEIL_MOVED);
            } else {
                player.sendTranslatable(NavigationMessages.CEIL_OBSTRUCTED);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

