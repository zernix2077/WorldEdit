package xyz.zernix.worldedit.command.general;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Snapshot;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.HistoryMessages;

/**
 * Reapplies recently undone WorldEdit snapshots.
 */
public final class RedoCommand extends WorldEditCommand {
    public RedoCommand() {
        super("redo", "worldedit:command.redo.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("count")
                .optional()
                .exec((context, player) -> {
                    int count = Math.max(1, context.getResult(0));
                    Snapshot.TraverseResult result = Snapshot.redo(player, count);
                    if (result.busy()) {
                        player.sendTranslatable(HistoryMessages.ERR_BUSY);
                    } else if (result.steps() == 0) {
                        player.sendTranslatable(HistoryMessages.REDO_NOTHING);
                    } else {
                        player.sendTranslatable(HistoryMessages.REDO_SUCCESS, result.steps(), result.changed());
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

