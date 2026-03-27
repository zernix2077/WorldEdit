package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Placeholder for region regeneration on unsupported backend.
 */
public final class RegenCommand extends WorldEditCommand {
    public RegenCommand() {
        super("regen", "worldedit:command.regen.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            player.sendTranslatable(RegionMessages.REGEN_UNAVAILABLE);
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

