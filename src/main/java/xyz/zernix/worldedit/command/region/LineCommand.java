package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3ic;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.command.arg.Flag;
import xyz.zernix.worldedit.command.arg.FlagsNode;
import xyz.zernix.worldedit.message.RegionMessages;
import xyz.zernix.worldedit.message.SelectionMessages;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Draws a line between selected points with optional thickness.
 */
public final class LineCommand extends RegionCommand {
    private enum LineFlag implements Flag {
        SHELL('h');

        private final char token;

        LineFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public LineCommand() {
        super("line", "worldedit:command.line.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var pattern = tree.getRoot().addLeaf(parent -> new BlockPatternNode("pattern", parent));

        pattern.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildLine(player, snapshot, context.getResult(0), 0, false)), SenderType.ACTUAL_PLAYER);

        var thickness = pattern.intNum("thickness");
        thickness.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                buildLine(player, snapshot, context.getResult(0), context.getResult(1), false)), SenderType.ACTUAL_PLAYER);

        pattern.addLeaf(parent -> new FlagsNode<>("flags", parent, LineFlag.class))
                .exec((context, player) -> {
                    Set<LineFlag> flags = context.getResult(1);
                    return runWithSnapshot(context, player, snapshot ->
                            buildLine(player, snapshot, context.getResult(0), 0, flags.contains(LineFlag.SHELL)));
                }, SenderType.ACTUAL_PLAYER);

        thickness.addLeaf(parent -> new FlagsNode<>("flags", parent, LineFlag.class))
                .exec((context, player) -> {
                    Set<LineFlag> flags = context.getResult(2);
                    return runWithSnapshot(context, player, snapshot -> buildLine(
                            player,
                            snapshot,
                            context.getResult(0),
                            context.getResult(1),
                            flags.contains(LineFlag.SHELL)
                    ));
                }, SenderType.ACTUAL_PLAYER);
    }

    private static void addThickPoint(LinkedHashSet<BlockPos> targets, List<Offset> offsets, int x, int y, int z) {
        for (Offset offset : offsets) {
            targets.add(new BlockPos(x + offset.x, y + offset.y, z + offset.z));
        }
    }

    private static void buildLine(
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            xyz.zernix.worldedit.Snapshot snapshot,
            BlockPattern pattern,
            int rawThickness,
            boolean shell
    ) {
        Selection.State state = Selection.of(player);
        Vector3ic a = state.pos1();
        Vector3ic b = state.pos2();
        if (a == null || b == null) {
            player.sendTranslatable(SelectionMessages.ERR_SET_REQUIRED_BOTH);
            return;
        }

        var dimension = player.getLocation().dimension();
        int radius = Math.max(0, rawThickness);
        LinkedHashSet<BlockPos> targets = new LinkedHashSet<>();

        int dx = b.x() - a.x();
        int dy = b.y() - a.y();
        int dz = b.z() - a.z();
        int steps = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));

        List<Offset> offsets = new ArrayList<>();
        if (radius <= 0) {
            offsets.add(new Offset(0, 0, 0));
        } else {
            int r2 = radius * radius;
            int inner = Math.max(0, radius - 1);
            int inner2 = inner * inner;
            for (int ox = -radius; ox <= radius; ox++) {
                for (int oy = -radius; oy <= radius; oy++) {
                    for (int oz = -radius; oz <= radius; oz++) {
                        int d2 = ox * ox + oy * oy + oz * oz;
                        if (d2 <= r2 && (!shell || d2 >= inner2)) {
                            offsets.add(new Offset(ox, oy, oz));
                        }
                    }
                }
            }
        }

        if (steps == 0) {
            addThickPoint(targets, offsets, a.x(), a.y(), a.z());
        } else {
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / (double) steps;
                int x = (int) Math.round(a.x() + dx * t);
                int y = (int) Math.round(a.y() + dy * t);
                int z = (int) Math.round(a.z() + dz * t);
                addThickPoint(targets, offsets, x, y, z);
            }
        }

        int total = 0;
        for (BlockPos target : targets) {
            if (dimension.isInWorld(target.x(), target.y(), target.z())) {
                snapshot.setBlockState(target.x(), target.y(), target.z(), pattern.next());
                total++;
            }
        }
        player.sendTranslatable(RegionMessages.LINE_SUCCESS, total);
    }

    private record BlockPos(int x, int y, int z) {
    }

    private record Offset(int x, int y, int z) {
    }
}
