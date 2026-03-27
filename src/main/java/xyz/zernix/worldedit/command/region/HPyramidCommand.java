package xyz.zernix.worldedit.command.region;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.BlockPatternNode;

/**
 * Creates a hollow pyramid at player position.
 */
public final class HPyramidCommand extends RegionCommand {
    public HPyramidCommand() {
        super("hpyramid", "worldedit:command.hpyramid.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new BlockPatternNode("pattern", parent))
                .intNum("size")
                .exec((context, player) -> {
                    BlockPattern pattern = context.getResult(0);
                    int size = context.getResult(1);
                    return runWithSnapshot(context, player, snapshot -> PyramidCommand.buildPyramid(player, snapshot, pattern, size, true));
                }, SenderType.ACTUAL_PLAYER);
    }
}

