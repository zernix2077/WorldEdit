package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.NavigationMessages;

/**
 * Escapes from being stuck inside blocks.
 */
public final class UnstuckCommand extends WorldEditCommand {
    public UnstuckCommand() {
        super("unstuck", "worldedit:command.unstuck.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            var location = player.getLocation();
            int x = (int) Math.floor(location.x());
            int y = (int) Math.floor(location.y());
            int z = (int) Math.floor(location.z());

            if (NavigationUtil.findFreePosition(player, x, y, z)) {
                player.sendTranslatable(NavigationMessages.UNSTUCK_MOVED);
            } else {
                player.sendTranslatable(NavigationMessages.UNSTUCK_FAILED);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

