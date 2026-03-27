package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.container.ContainerTypes;
import org.allaymc.api.item.type.ItemTypes;
import org.allaymc.api.message.I18n;
import org.allaymc.api.pdc.PersistentDataType;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.listener.WandListener;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Gives the caller a WorldEdit wand used to set selection points by interaction.
 */
public final class WandCommand extends WorldEditCommand {
    public WandCommand() {
        super("wand", "worldedit:command.wand.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            var wand = ItemTypes.WOODEN_AXE.createItemStack();
            wand.setCustomName(I18n.get().tr(SelectionMessages.WAND_NAME));
            wand.getPersistentDataContainer().set(WandListener.WAND_TAG, PersistentDataType.BOOLEAN, true);

            if (player.getContainer(ContainerTypes.INVENTORY).tryAddItem(wand) != -1) {
                player.sendTranslatable(SelectionMessages.WAND_GIVE_SUCCESS);
            } else {
                player.sendTranslatable(SelectionMessages.WAND_GIVE_FAILURE);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

