package xyz.zernix.worldedit.command.region;

import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import xyz.zernix.worldedit.Clipboard;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.Snapshot;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Copies selected region to clipboard and replaces source with fill blocks.
 */
public final class CutCommand extends RegionCommand {
    public CutCommand() {
        super("cut", "worldedit:command.cut.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("fill", parent))
                .optional()
                .exec((context, player) -> {
                    BlockPattern fill = context.getResult(0);
                    return runWithSnapshot(context, player, snapshot -> {
                        Selection.State state = Selection.of(player);
                        Vector3ic pos1 = state.pos1();
                        Vector3ic pos2 = state.pos2();
                        if (pos1 == null || pos2 == null) {
                            player.sendTranslatable(SelectionMessages.ERR_SET_REQUIRED_BOTH);
                            return;
                        }

                        var dimension = player.getLocation().dimension();
                        if (!dimension.isInWorld(pos1.x(), pos1.y(), pos1.z()) || !dimension.isInWorld(pos2.x(), pos2.y(), pos2.z())) {
                            player.sendTranslatable(RegionMessages.ERR_POS_OUTSIDE_WORLD);
                            return;
                        }

                        int minX = Math.min(pos1.x(), pos2.x());
                        int maxX = Math.max(pos1.x(), pos2.x());
                        int minY = Math.min(pos1.y(), pos2.y());
                        int maxY = Math.max(pos1.y(), pos2.y());
                        int minZ = Math.min(pos1.z(), pos2.z());
                        int maxZ = Math.max(pos1.z(), pos2.z());

                        Snapshot clip = Snapshot.take(dimension, out -> {
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
                        Clipboard.set(player, clip, origin);

                        var air = BlockTypes.AIR.getDefaultState();
                        for (int x = minX; x <= maxX; x++) {
                            for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    snapshot.setBlockState(x, y, z, fill == null ? air : fill.next());
                                }
                            }
                        }

                        player.sendTranslatable(RegionMessages.CUT_SUCCESS, clip.size());
                    });
                }, SenderType.ACTUAL_PLAYER);
    }
}

