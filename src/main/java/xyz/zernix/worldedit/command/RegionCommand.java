package xyz.zernix.worldedit.command;

import org.allaymc.api.command.CommandResult;
import org.allaymc.api.command.tree.CommandContext;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import xyz.zernix.worldedit.Snapshot;
import xyz.zernix.worldedit.message.HistoryMessages;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shared root for region commands that mutate the world and participate in history.
 */
public abstract class RegionCommand extends WorldEditCommand {
    protected RegionCommand(String name, String descriptionKey) {
        super(name, descriptionKey);
    }

    protected RegionCommand(String name, String descriptionKey, List<String> aliases) {
        super(name, descriptionKey, aliases);
    }

    /**
     * Runs mutation in a snapshot transaction and stores it in per-player history.
     */
    protected CommandResult runWithSnapshot(CommandContext context, EntityPlayer player, Consumer<Snapshot> run) {
        Snapshot.RecordResult result = Snapshot.record(player, run);
        if (result.busy()) {
            player.sendTranslatable(HistoryMessages.ERR_BUSY);
            return context.fail();
        }
        return context.success();
    }
}

