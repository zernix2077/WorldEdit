package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.NavigationMessages;

/**
 * Moves upwards by a distance.
 */
public final class UpCommand extends WorldEditCommand {
    public UpCommand() {
        super("up", "worldedit:command.up.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("distance")
                .exec((context, player) -> {
                    int distance = context.getResult(0);
                    var location = player.getLocation();
                    var dimension = location.dimension();
                    int x = (int) Math.floor(location.x());
                    int z = (int) Math.floor(location.z());
                    int minHeight = dimension.getDimensionInfo().minHeight();
                    int maxHeight = dimension.getDimensionInfo().maxHeight();
                    int initialY = Math.max(minHeight, (int) Math.floor(location.y()));
                    int maxY = Math.min(maxHeight + 1, initialY + Math.max(0, distance));
                    int startY = Math.max(minHeight, initialY + 1);

                    boolean moved = false;
                    for (int y = startY; y <= maxHeight + 2; y++) {
                        if (NavigationUtil.isMovementBlocker(dimension.getBlockState(x, y, z)) || y > maxY + 1) {
                            moved = (y == maxY + 1) && NavigationUtil.teleportCentered(player, x, maxY, z);
                            break;
                        }
                    }

                    if (moved) {
                        player.sendTranslatable(NavigationMessages.UP_MOVED);
                    } else {
                        player.sendTranslatable(NavigationMessages.UP_OBSTRUCTED);
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

