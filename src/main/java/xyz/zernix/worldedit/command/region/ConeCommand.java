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
 * Creates a cone at player position.
 */
public final class ConeCommand extends RegionCommand {
    private enum ConeFlag implements Flag {
        HOLLOW('h');

        private final char token;

        ConeFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public ConeCommand() {
        super("cone", "worldedit:command.cone.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var radii = tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .str("radii");
        radii.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildCone(player, snapshot, context.getResult(0), context.getResult(1), new Parsed(null, null, Set.of()))), SenderType.ACTUAL_PLAYER);

        var height = radii.intNum("height");
        height.exec((context, player) -> runWithSnapshot(context, player, snapshot -> buildCone(
                player,
                snapshot,
                context.getResult(0),
                context.getResult(1),
                new Parsed(context.getResult(2), null, Set.of())
        )), SenderType.ACTUAL_PLAYER);

        var thickness = height.intNum("thickness");
        thickness.exec((context, player) -> runWithSnapshot(context, player, snapshot -> buildCone(
                player,
                snapshot,
                context.getResult(0),
                context.getResult(1),
                new Parsed(context.getResult(2), context.getResult(3), Set.of())
        )), SenderType.ACTUAL_PLAYER);

        radii.addLeaf(parent -> new FlagsNode<>("flags", parent, ConeFlag.class))
                .exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                        buildCone(player, snapshot, context.getResult(0), context.getResult(1), new Parsed(null, null, context.getResult(2)))),
                        SenderType.ACTUAL_PLAYER);

        height.addLeaf(parent -> new FlagsNode<>("flags", parent, ConeFlag.class))
                .exec((context, player) -> runWithSnapshot(context, player, snapshot -> buildCone(
                        player,
                        snapshot,
                        context.getResult(0),
                        context.getResult(1),
                        new Parsed(context.getResult(2), null, context.getResult(3))
                )), SenderType.ACTUAL_PLAYER);

        thickness.addLeaf(parent -> new FlagsNode<>("flags", parent, ConeFlag.class))
                .exec((context, player) -> runWithSnapshot(context, player, snapshot -> buildCone(
                        player,
                        snapshot,
                        context.getResult(0),
                        context.getResult(1),
                        new Parsed(context.getResult(2), context.getResult(3), context.getResult(4))
                )), SenderType.ACTUAL_PLAYER);
    }

    private void buildCone(
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            xyz.zernix.worldedit.Snapshot snapshot,
            BlockPattern pattern,
            String radiiRaw,
            Parsed parsed
    ) {
        ShapeArgs.XZ radii;
        try {
            radii = ShapeArgs.parseXZ(radiiRaw);
        } catch (ArgumentParseException exception) {
            player.sendTranslatable(exception.key(), exception.args());
            return;
        }

        int baseRx = radii.rx();
        int baseRz = radii.rz();
        var location = player.getLocation();
        var dimension = location.dimension();
        int cx = (int) Math.floor(location.x());
        int cy = (int) Math.floor(location.y());
        int cz = (int) Math.floor(location.z());
        int height = Math.max(1, parsed.height() == null ? Math.max(baseRx, baseRz) : parsed.height());
        boolean hollow = parsed.flags().contains(ConeFlag.HOLLOW);
        int thickness = Math.max(1, parsed.thickness() == null ? 1 : parsed.thickness());

        int total = 0;
        for (int yOff = 0; yOff < height; yOff++) {
            double t = (double) yOff / (double) height;
            int rx = Math.max(1, (int) Math.ceil(baseRx * (1d - t)));
            int rz = Math.max(1, (int) Math.ceil(baseRz * (1d - t)));
            long rx2 = (long) rx * rx;
            long rz2 = (long) rz * rz;
            int innerRx = Math.max(0, rx - thickness);
            int innerRz = Math.max(0, rz - thickness);
            long innerRx2 = (long) innerRx * innerRx;
            long innerRz2 = (long) innerRz * innerRz;
            int y = cy + yOff;

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

        player.sendTranslatable(RegionMessages.CONE_SUCCESS, total);
    }
    private record Parsed(Integer height, Integer thickness, Set<ConeFlag> flags) {
    }
}
