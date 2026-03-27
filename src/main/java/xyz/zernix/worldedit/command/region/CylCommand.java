package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.ArgumentParseException;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.command.arg.Flag;
import xyz.zernix.worldedit.command.arg.FlagsNode;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.Set;

/**
 * Creates a cylinder at player position.
 */
public final class CylCommand extends RegionCommand {
    private enum CylFlag implements Flag {
        HOLLOW('h');

        private final char token;

        CylFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public CylCommand() {
        super("cyl", "worldedit:command.cyl.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var radii = tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .str("radii");

        radii.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildCylinder(player, snapshot, context.getResult(0), context.getResult(1), 1, false)), SenderType.ACTUAL_PLAYER);

        var height = radii.intNum("height");
        height.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildCylinder(player, snapshot, context.getResult(0), context.getResult(1), context.getResult(2), false)), SenderType.ACTUAL_PLAYER);

        radii.addLeaf(parent -> new FlagsNode<>("flags", parent, CylFlag.class))
                .exec((context, player) -> {
                    Set<CylFlag> flags = context.getResult(2);
                    return runWithSnapshot(context, player, snapshot -> buildCylinder(
                            player,
                            snapshot,
                            context.getResult(0),
                            context.getResult(1),
                            1,
                            flags.contains(CylFlag.HOLLOW)
                    ));
                }, SenderType.ACTUAL_PLAYER);

        height.addLeaf(parent -> new FlagsNode<>("flags", parent, CylFlag.class))
                .exec((context, player) -> {
                    Set<CylFlag> flags = context.getResult(3);
                    return runWithSnapshot(context, player, snapshot -> buildCylinder(
                            player,
                            snapshot,
                            context.getResult(0),
                            context.getResult(1),
                            context.getResult(2),
                            flags.contains(CylFlag.HOLLOW)
                    ));
                }, SenderType.ACTUAL_PLAYER);
    }

    static void buildCylinder(
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            xyz.zernix.worldedit.Snapshot snapshot,
            BlockPattern pattern,
            String radiiRaw,
            int heightRaw,
            boolean hollow
    ) {
        ShapeArgs.XZ radii;
        try {
            radii = ShapeArgs.parseXZ(radiiRaw);
        } catch (ArgumentParseException exception) {
            player.sendTranslatable(exception.key(), exception.args());
            return;
        }

        int rx = radii.rx();
        int rz = radii.rz();
        var location = player.getLocation();
        var dimension = location.dimension();
        int cx = (int) Math.floor(location.x());
        int cy = (int) Math.floor(location.y());
        int cz = (int) Math.floor(location.z());
        int height = Math.max(1, heightRaw);

        long rx2 = (long) rx * rx;
        long rz2 = (long) rz * rz;
        int innerRx = Math.max(0, rx - 1);
        int innerRz = Math.max(0, rz - 1);
        long innerRx2 = (long) innerRx * innerRx;
        long innerRz2 = (long) innerRz * innerRz;

        int total = 0;
        for (int y = cy; y < cy + height; y++) {
            for (int x = cx - rx; x <= cx + rx; x++) {
                for (int z = cz - rz; z <= cz + rz; z++) {
                    long dx = x - cx;
                    long dz = z - cz;
                    boolean outer = dx * dx * rz2 + dz * dz * rx2 <= rx2 * rz2;
                    boolean inner = dx * dx * innerRz2 + dz * dz * innerRx2 < innerRx2 * innerRz2;
                    if (outer && (!hollow || !inner) && dimension.isInWorld(x, y, z)) {
                        snapshot.setBlockState(x, y, z, pattern.next());
                        total++;
                    }
                }
            }
        }

        player.sendTranslatable(RegionMessages.CYLINDER_SUCCESS, total);
    }
}
