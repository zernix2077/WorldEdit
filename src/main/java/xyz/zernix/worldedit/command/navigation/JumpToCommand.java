package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.math.MathUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.NavigationMessages;

import java.util.List;

/**
 * Teleports to looked-at location.
 */
public final class JumpToCommand extends WorldEditCommand {
    public JumpToCommand() {
        super("jumpto", "worldedit:command.jumpto.description", List.of("j"));
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            var location = player.getLocation();
            var dimension = location.dimension();
            Vector3d origin = new Vector3d(location.x(), location.y() + player.getEyeHeight(), location.z());
            Vector3d direction = MathUtils.getDirectionVector(location.yaw(), location.pitch());

            Vector3i target = null;
            for (double t = 0d; t <= 300d; t += NavigationUtil.TRACE_STEP) {
                int bx = (int) Math.floor(origin.x + direction.x * t);
                int by = (int) Math.floor(origin.y + direction.y * t);
                int bz = (int) Math.floor(origin.z + direction.z * t);
                if (!dimension.isInWorld(bx, by, bz)) {
                    continue;
                }
                if (NavigationUtil.isMovementBlocker(dimension.getBlockState(bx, by, bz))) {
                    target = new Vector3i(bx, by, bz);
                    break;
                }
            }

            if (target == null) {
                player.sendTranslatable(NavigationMessages.JUMP_TO_NONE);
            } else if (NavigationUtil.findFreePosition(player, target.x, target.y, target.z)) {
                player.sendTranslatable(NavigationMessages.JUMP_TO_MOVED);
            } else {
                player.sendTranslatable(NavigationMessages.JUMP_TO_OBSTRUCTED);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

