package xyz.zernix.worldedit.command.clipboard;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Clipboard;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.ClipboardMessages;

/**
 * Clears player's clipboard.
 */
public final class ClearClipboardCommand extends WorldEditCommand {
    public ClearClipboardCommand() {
        super("clearclipboard", "worldedit:command.clearclipboard.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            if (Clipboard.clear(player)) {
                player.sendTranslatable(ClipboardMessages.CLEARED);
            } else {
                player.sendTranslatable(ClipboardMessages.ALREADY_EMPTY);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

