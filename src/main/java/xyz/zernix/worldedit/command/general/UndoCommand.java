package xyz.zernix.worldedit.command.general;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Snapshot;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.HistoryMessages;

/**
 * Reverts recent WorldEdit snapshots.
 */
public final class UndoCommand extends WorldEditCommand {
    public UndoCommand() {
        super("undo", "worldedit:command.undo.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("count")
                .optional()
                .exec((context, player) -> {
                    int count = Math.max(1, context.getResult(0));
                    Snapshot.TraverseResult result = Snapshot.undo(player, count);
                    if (result.busy()) {
                        player.sendTranslatable(HistoryMessages.ERR_BUSY);
                    } else if (result.steps() == 0) {
                        player.sendTranslatable(HistoryMessages.UNDO_NOTHING);
                    } else {
                        player.sendTranslatable(HistoryMessages.UNDO_SUCCESS, result.steps(), result.changed());
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

