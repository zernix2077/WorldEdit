package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.math.MathUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.SelectionMessages;

/**
 * Base command that raycasts and sets one selection corner to looked-at block.
 */
abstract class SetHPosCommand extends WorldEditCommand {
    private final Selection.PosKind kind;

    protected SetHPosCommand(String name, String descriptionKey, Selection.PosKind kind) {
        super(name, descriptionKey);
        this.kind = kind;
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            var location = player.getLocation();
            var dimension = location.dimension();
            Vector3d origin = new Vector3d(location.x(), location.y() + player.getEyeHeight(), location.z());
            Vector3d direction = MathUtils.getDirectionVector(location.yaw(), location.pitch());

            Vector3i last = null;
            Vector3i target = null;
            for (double t = 0d; t <= 300d; t += 0.2d) {
                int bx = (int) Math.floor(origin.x + direction.x * t);
                int by = (int) Math.floor(origin.y + direction.y * t);
                int bz = (int) Math.floor(origin.z + direction.z * t);
                if (!dimension.isInWorld(bx, by, bz)) {
                    continue;
                }

                Vector3i current = new Vector3i(bx, by, bz);
                if (last != null && last.equals(current)) {
                    continue;
                }
                last = current;

                if (dimension.getBlockState(bx, by, bz).getBlockStateData().hasCollision()) {
                    target = current;
                    break;
                }
            }

            if (target != null) {
                Selection.setPos(player, kind, target);
            } else {
                player.sendTranslatable(SelectionMessages.HPOS_NO_BLOCK);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

