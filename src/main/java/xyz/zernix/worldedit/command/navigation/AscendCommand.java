package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.NavigationMessages;

import java.util.List;

/**
 * Ascends specified amount of levels.
 */
public final class AscendCommand extends WorldEditCommand {
    public AscendCommand() {
        super("ascend", "worldedit:command.ascend.description", List.of("asc"));
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("levels")
                .optional()
                .exec((context, player) -> {
                    int target = Math.max(1, context.getResult(0));
                    int done = 0;

                    for (int i = 0; i < target; i++) {
                        var location = player.getLocation();
                        var dimension = location.dimension();
                        int x = (int) Math.floor(location.x());
                        int z = (int) Math.floor(location.z());
                        int y = Math.max(dimension.getDimensionInfo().minHeight(), (int) Math.floor(location.y()) + 1);
                        int maxY = Math.min(dimension.getDimensionInfo().maxHeight(), y + NavigationUtil.VERTICAL_SEARCH_HEIGHT) + 2;

                        boolean moved = false;
                        for (int yy = y; yy <= maxY; yy++) {
                            if (NavigationUtil.safeForStanding(dimension, x, yy, z) && NavigationUtil.teleportCentered(player, x, yy, z)) {
                                moved = true;
                                break;
                            }
                        }
                        if (!moved) {
                            break;
                        }
                        done++;
                    }

                    if (done == 0) {
                        player.sendTranslatable(NavigationMessages.ASCEND_OBSTRUCTED);
                    } else {
                        player.sendTranslatable(NavigationMessages.ASCEND_MOVED, done);
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

