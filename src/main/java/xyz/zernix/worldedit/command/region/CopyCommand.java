package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import xyz.zernix.worldedit.Clipboard;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.Snapshot;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.RegionMessages;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Copies selected region into player's clipboard.
 */
public final class CopyCommand extends WorldEditCommand {
    public CopyCommand() {
        super("copy", "worldedit:command.copy.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            Selection.State state = Selection.of(player);
            Vector3ic pos1 = state.pos1();
            Vector3ic pos2 = state.pos2();
            if (pos1 == null || pos2 == null) {
                player.sendTranslatable(SelectionMessages.ERR_SET_REQUIRED_BOTH);
                return context.success();
            }

            var dimension = player.getLocation().dimension();
            if (!dimension.isInWorld(pos1.x(), pos1.y(), pos1.z()) || !dimension.isInWorld(pos2.x(), pos2.y(), pos2.z())) {
                player.sendTranslatable(RegionMessages.ERR_POS_OUTSIDE_WORLD);
                return context.success();
            }

            int minX = Math.min(pos1.x(), pos2.x());
            int maxX = Math.max(pos1.x(), pos2.x());
            int minY = Math.min(pos1.y(), pos2.y());
            int maxY = Math.max(pos1.y(), pos2.y());
            int minZ = Math.min(pos1.z(), pos2.z());
            int maxZ = Math.max(pos1.z(), pos2.z());

            Snapshot snapshot = Snapshot.take(dimension, out -> {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            out.record(x, y, z, dimension.getBlockState(x, y, z));
                        }
                    }
                }
            });

            var executeLocation = player.getCommandExecuteLocation();
            Vector3i origin = new Vector3i(
                    (int) Math.floor(executeLocation.x()),
                    (int) Math.floor(executeLocation.y()),
                    (int) Math.floor(executeLocation.z())
            );
            Clipboard.set(player, snapshot, origin);
            player.sendTranslatable(RegionMessages.COPY_SUCCESS, snapshot.size());
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

