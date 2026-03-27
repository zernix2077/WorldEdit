package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.command.arg.Direction;
import xyz.zernix.worldedit.command.arg.DirectionNode;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Moves the whole selection by offset along a direction.
 */
public final class ShiftCommand extends WorldEditCommand {
    public ShiftCommand() {
        super("shift", "worldedit:command.shift.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("amount")
                .addLeaf(parent -> new DirectionNode("direction", parent))
                .optional()
                .exec((context, player) -> {
                    Selection.Bounds bounds = Selection.bounds(player);
                    if (bounds == null) {
                        return context.success();
                    }

                    int amount = context.getResult(0);
                    Direction direction = context.getResult(1);
                    Direction resolved = Direction.resolve(player, direction);
                    Vector3i delta = Direction.unit(resolved).mul(amount);
                    Selection.Bounds out = bounds.shifted(delta.x, delta.y, delta.z);
                    if (Selection.applyBounds(player, out)) {
                        player.sendTranslatable(SelectionMessages.SHIFTED);
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

