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
 * Creates a sphere at player position.
 */
public final class SphereCommand extends RegionCommand {
    private enum SphereFlag implements Flag {
        RAISED('r'),
        HOLLOW('h');

        private final char token;

        SphereFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public SphereCommand() {
        super("sphere", "worldedit:command.sphere.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var radii = tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .str("radii");

        radii.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildSphere(player, snapshot, context.getResult(0), context.getResult(1), false, false)), SenderType.ACTUAL_PLAYER);

        radii.addLeaf(parent -> new FlagsNode<>("flags", parent, SphereFlag.class))
                .exec((context, player) -> {
                    Set<SphereFlag> flags = context.getResult(2);
                    return runWithSnapshot(context, player, snapshot -> buildSphere(
                            player,
                            snapshot,
                            context.getResult(0),
                            context.getResult(1),
                            flags.contains(SphereFlag.HOLLOW),
                            flags.contains(SphereFlag.RAISED)
                    ));
                }, SenderType.ACTUAL_PLAYER);
    }

    static void buildSphere(
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            xyz.zernix.worldedit.Snapshot snapshot,
            BlockPattern pattern,
            String radiiRaw,
            boolean hollow,
            boolean raised
    ) {
        ShapeArgs.XYZ radii;
        try {
            radii = ShapeArgs.parseXYZ(radiiRaw);
        } catch (ArgumentParseException exception) {
            player.sendTranslatable(exception.key(), exception.args());
            return;
        }

        int rx = radii.rx();
        int ry = radii.ry();
        int rz = radii.rz();
        var location = player.getLocation();
        var dimension = location.dimension();
        int cx = (int) Math.floor(location.x());
        int baseY = (int) Math.floor(location.y());
        int cz = (int) Math.floor(location.z());
        int cy = raised ? baseY + ry : baseY;

        int innerRx = Math.max(0, rx - 1);
        int innerRy = Math.max(0, ry - 1);
        int innerRz = Math.max(0, rz - 1);
        long rx2 = (long) rx * rx;
        long ry2 = (long) ry * ry;
        long rz2 = (long) rz * rz;
        long innerRx2 = (long) innerRx * innerRx;
        long innerRy2 = (long) innerRy * innerRy;
        long innerRz2 = (long) innerRz * innerRz;

        int total = 0;
        for (int x = cx - rx; x <= cx + rx; x++) {
            for (int y = cy - ry; y <= cy + ry; y++) {
                for (int z = cz - rz; z <= cz + rz; z++) {
                    long dx = x - cx;
                    long dy = y - cy;
                    long dz = z - cz;
                    boolean outer = dx * dx * ry2 * rz2 + dy * dy * rx2 * rz2 + dz * dz * rx2 * ry2 <= rx2 * ry2 * rz2;
                    boolean inner = dx * dx * innerRy2 * innerRz2 + dy * dy * innerRx2 * innerRz2 + dz * dz * innerRx2 * innerRy2
                            < innerRx2 * innerRy2 * innerRz2;
                    if (outer && (!hollow || !inner) && dimension.isInWorld(x, y, z)) {
                        snapshot.setBlockState(x, y, z, pattern.next());
                        total++;
                    }
                }
            }
        }

        player.sendTranslatable(RegionMessages.SPHERE_SUCCESS, total);
    }
}

