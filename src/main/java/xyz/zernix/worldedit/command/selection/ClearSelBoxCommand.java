package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Clears currently rendered selection box.
 */
public final class ClearSelBoxCommand extends WorldEditCommand {
    public ClearSelBoxCommand() {
        super("clearselbox", "worldedit:command.clearselbox.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            Selection.of(player).clearVisual(player);
            player.sendTranslatable(SelectionMessages.CLEARED);
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

