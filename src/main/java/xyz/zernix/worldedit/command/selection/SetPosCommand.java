package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3d;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;

/**
 * Base command that sets one selection corner from explicit coordinates or player position.
 */
abstract class SetPosCommand extends WorldEditCommand {
    private final Selection.PosKind kind;

    protected SetPosCommand(String name, String descriptionKey, Selection.PosKind kind) {
        super(name, descriptionKey);
        this.kind = kind;
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .pos("pos")
                .optional()
                .exec((context, player) -> {
                    Vector3d position = context.getResult(0);
                    if (position == null) {
                        var executeLocation = player.getCommandExecuteLocation();
                        position = new Vector3d(executeLocation.x(), executeLocation.y(), executeLocation.z());
                    }

                    Vector3i blockPos = new Vector3i(
                            (int) Math.floor(position.x),
                            (int) Math.floor(position.y),
                            (int) Math.floor(position.z)
                    );
                    Selection.setPos(player, kind, blockPos);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

