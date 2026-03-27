package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Clipboard;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Pastes clipboard relative to current command position.
 */
public final class PasteCommand extends RegionCommand {
    public PasteCommand() {
        super("paste", "worldedit:command.paste.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> runWithSnapshot(context, player, snapshot -> {
            Clipboard.Entry entry = Clipboard.get(player);
            if (entry == null) {
                player.sendTranslatable(RegionMessages.ERR_CLIPBOARD_EMPTY);
                return;
            }

            var executeLocation = player.getCommandExecuteLocation();
            Vector3i origin = new Vector3i(
                    (int) Math.floor(executeLocation.x()),
                    (int) Math.floor(executeLocation.y()),
                    (int) Math.floor(executeLocation.z())
            );
            int dx = origin.x - entry.origin().x();
            int dy = origin.y - entry.origin().y();
            int dz = origin.z - entry.origin().z();

            var dimension = player.getLocation().dimension();
            final int[] counts = new int[2];
            entry.snapshot().forEachAfter((x, y, z, block) -> {
                int tx = x + dx;
                int ty = y + dy;
                int tz = z + dz;
                if (dimension.isInWorld(tx, ty, tz)) {
                    snapshot.setBlockState(tx, ty, tz, block);
                    counts[0]++;
                } else {
                    counts[1]++;
                }
            });
            int written = counts[0];
            int skipped = counts[1];

            if (skipped == 0) {
                player.sendTranslatable(RegionMessages.PASTE_SUCCESS, written);
            } else {
                player.sendTranslatable(RegionMessages.PASTE_PARTIAL_SUCCESS, written, skipped);
            }
        }), SenderType.ACTUAL_PLAYER);
    }
}

