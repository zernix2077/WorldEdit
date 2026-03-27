package xyz.zernix.worldedit.listener;

import org.allaymc.api.container.ContainerTypes;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.block.BlockBreakEvent;
import org.allaymc.api.eventbus.event.player.PlayerInteractBlockEvent;
import org.allaymc.api.eventbus.event.player.PlayerPunchBlockEvent;
import org.allaymc.api.pdc.PersistentDataType;
import org.allaymc.api.permission.Tristate;
import org.allaymc.api.utils.identifier.Identifier;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.WorldEdit;

/**
 * Listener that handles WorldEdit wand interactions.
 */
public final class WandListener {
    /**
     * Persistent item tag used to identify a WorldEdit wand.
     */
    public static final Identifier WAND_TAG = new Identifier("zernix", "worldedit_wand");

    private boolean holdsWand(EntityPlayer player) {
        var item = player.getContainer(ContainerTypes.INVENTORY).getItemInHand();
        return item.getPersistentDataContainer().has(WAND_TAG, PersistentDataType.BOOLEAN)
                && player.hasPermission(WorldEdit.PERMISSION) == Tristate.TRUE;
    }

    @EventHandler
    private void onClick(PlayerInteractBlockEvent event) {
        if (!holdsWand(event.getPlayer())) {
            return;
        }

        event.cancel();
        Selection.PosKind kind = event.getAction() == PlayerInteractBlockEvent.Action.LEFT_CLICK
                ? Selection.PosKind.FIRST
                : Selection.PosKind.SECOND;
        Selection.setPos(event.getPlayer(), kind, event.getInteractInfo().clickedBlockPos(), true);
    }

    @EventHandler
    private void onBlockPunch(PlayerPunchBlockEvent event) {
        if (!holdsWand(event.getPlayer())) {
            return;
        }

        event.cancel();
        Selection.setFirst(event.getPlayer(), event.getBlock().getPosition(), true);
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getEntity() instanceof EntityPlayer player && holdsWand(player)) {
            event.cancel();
        }
    }
}

