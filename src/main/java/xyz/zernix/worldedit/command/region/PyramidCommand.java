package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import xyz.zernix.worldedit.Snapshot;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.command.arg.Flag;
import xyz.zernix.worldedit.command.arg.FlagsNode;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.Set;

/**
 * Creates a pyramid at player position.
 */
public final class PyramidCommand extends RegionCommand {
    private enum PyramidFlag implements Flag {
        HOLLOW('h');

        private final char token;

        PyramidFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public PyramidCommand() {
        super("pyramid", "worldedit:command.pyramid.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var size = tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .intNum("size");

        size.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildPyramid(player, snapshot, context.getResult(0), context.getResult(1), false)), SenderType.ACTUAL_PLAYER);

        size.addLeaf(parent -> new FlagsNode<>("flags", parent, PyramidFlag.class))
                .exec((context, player) -> {
                    Set<PyramidFlag> flags = context.getResult(2);
                    return runWithSnapshot(context, player, snapshot -> buildPyramid(
                            player,
                            snapshot,
                            context.getResult(0),
                            context.getResult(1),
                            flags.contains(PyramidFlag.HOLLOW)
                    ));
                }, SenderType.ACTUAL_PLAYER);
    }

    static void buildPyramid(
            EntityPlayer player,
            Snapshot snapshot,
            BlockPattern pattern,
            int sizeRaw,
            boolean hollow
    ) {
        var location = player.getLocation();
        var dimension = location.dimension();
        int cx = (int) Math.floor(location.x());
        int cy = (int) Math.floor(location.y());
        int cz = (int) Math.floor(location.z());
        int size = Math.max(1, sizeRaw);

        int total = 0;
        for (int layer = 0; layer < size; layer++) {
            int radius = size - layer - 1;
            int y = cy + layer;
            int minX = cx - radius;
            int maxX = cx + radius;
            int minZ = cz - radius;
            int maxZ = cz + radius;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean border = x == minX || x == maxX || z == minZ || z == maxZ || layer == size - 1;
                    if ((!hollow || border) && dimension.isInWorld(x, y, z)) {
                        snapshot.setBlockState(x, y, z, pattern.next());
                        total++;
                    }
                }
            }
        }

        player.sendTranslatable(RegionMessages.PYRAMID_SUCCESS, total);
    }
}

