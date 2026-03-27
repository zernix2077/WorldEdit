package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3ic;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.message.RegionMessages;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Fills selected area with a block pattern.
 */
public final class SetCommand extends RegionCommand {
    public SetCommand() {
        super("set", "worldedit:command.set.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .exec((context, player) -> {
                    BlockPattern pattern = context.getResult(0);
                    return runWithSnapshot(context, player, snapshot -> {
                        Selection.State state = Selection.of(player);
                        Vector3ic pos1 = state.pos1();
                        Vector3ic pos2 = state.pos2();
                        if (pos1 == null || pos2 == null) {
                            player.sendTranslatable(SelectionMessages.ERR_SET_REQUIRED_BOTH);
                            return;
                        }

                        int minX = Math.min(pos1.x(), pos2.x());
                        int maxX = Math.max(pos1.x(), pos2.x());
                        int minY = Math.min(pos1.y(), pos2.y());
                        int maxY = Math.max(pos1.y(), pos2.y());
                        int minZ = Math.min(pos1.z(), pos2.z());
                        int maxZ = Math.max(pos1.z(), pos2.z());

                        for (int x = minX; x <= maxX; x++) {
                            for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    snapshot.setBlockState(x, y, z, pattern.next());
                                }
                            }
                        }
                        int total = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                        player.sendTranslatable(RegionMessages.SET_SUCCESS, total);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }
}

