package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Toggles automatic rendering of the selection box.
 */
public final class SelBoxToggleCommand extends WorldEditCommand {
    public SelBoxToggleCommand() {
        super("selboxtoggle", "worldedit:command.selboxtoggle.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            boolean visible = Selection.of(player).toggleVisual(player);
            if (visible) {
                player.sendTranslatable(SelectionMessages.TOGGLE_ON);
            } else {
                player.sendTranslatable(SelectionMessages.TOGGLE_OFF);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

