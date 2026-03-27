package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;

import java.util.List;

/**
 * Creates a hollow cylinder at player position.
 */
public final class HCylCommand extends RegionCommand {
    public HCylCommand() {
        super("hcyl", "worldedit:command.hcyl.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .str("radii")
                .intNum("height")
                .optional()
                .exec((context, player) -> {
                    BlockPattern pattern = context.getResult(0);
                    String radii = context.getResult(1);
                    int height = context.getResult(2);
                    return runWithSnapshot(context, player, snapshot -> CylCommand.buildCylinder(player, snapshot, pattern, radii, height == 0 ? 1 : height, true));
                }, SenderType.ACTUAL_PLAYER);
    }
}

