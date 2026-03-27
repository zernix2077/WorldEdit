package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;
import xyz.zernix.worldedit.command.arg.Flag;
import xyz.zernix.worldedit.command.arg.FlagsNode;

import java.util.List;
import java.util.Set;

/**
 * Creates a hollow sphere at player position.
 */
public final class HSphereCommand extends RegionCommand {
    private enum HSphereFlag implements Flag {
        RAISED('r');

        private final char token;

        HSphereFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public HSphereCommand() {
        super("hsphere", "worldedit:command.hsphere.description", List.of("hpshere"));
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var radii = tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .str("radii");

        radii.exec((context, player) -> runWithSnapshot(context, player, snapshot ->
                SphereCommand.buildSphere(player, snapshot, context.getResult(0), context.getResult(1), true, false)), SenderType.ACTUAL_PLAYER);

        radii.addLeaf(parent -> new FlagsNode<>("flags", parent, HSphereFlag.class))
                .exec((context, player) -> {
                    Set<HSphereFlag> flags = context.getResult(2);
                    return runWithSnapshot(context, player, snapshot -> SphereCommand.buildSphere(
                            player,
                            snapshot,
                            context.getResult(0),
                            context.getResult(1),
                            true,
                            flags.contains(HSphereFlag.RAISED)
                    ));
                }, SenderType.ACTUAL_PLAYER);
    }
}
