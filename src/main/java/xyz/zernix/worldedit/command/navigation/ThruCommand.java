package xyz.zernix.worldedit.command.navigation;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.math.MathUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.NavigationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Passes through nearby walls.
 */
public final class ThruCommand extends WorldEditCommand {
    public ThruCommand() {
        super("thru", "worldedit:command.thru.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot().exec((context, player) -> {
            var location = player.getLocation();
            var dimension = location.dimension();
            Vector3d origin = new Vector3d(location.x(), location.y() + player.getEyeHeight(), location.z());
            Vector3d direction = MathUtils.getDirectionVector(location.yaw(), location.pitch());

            List<Vector3i> sampled = new ArrayList<>();
            Vector3i last = null;
            for (double t = 0d; t <= 6d; t += NavigationUtil.TRACE_STEP) {
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
                sampled.add(current);
                last = current;
            }

            int firstSolid = -1;
            for (int i = 0; i < sampled.size(); i++) {
                Vector3i pos = sampled.get(i);
                if (NavigationUtil.isMovementBlocker(dimension.getBlockState(pos.x, pos.y, pos.z))) {
                    firstSolid = i;
                    break;
                }
            }

            boolean moved = false;
            if (firstSolid >= 0) {
                Vector3i target = null;
                for (int i = firstSolid; i < sampled.size(); i++) {
                    Vector3i pos = sampled.get(i);
                    if (!NavigationUtil.isMovementBlocker(dimension.getBlockState(pos.x, pos.y, pos.z))) {
                        target = pos;
                        break;
                    }
                }

                if (target != null) {
                    int yy = Math.max(dimension.getDimensionInfo().minHeight(), target.y);
                    int minY = Math.max(dimension.getDimensionInfo().minHeight(), yy - NavigationUtil.VERTICAL_SEARCH_HEIGHT);
                    for (int y = yy; y >= minY; y--) {
                        if (NavigationUtil.isMovementBlocker(dimension.getBlockState(target.x, y, target.z))) {
                            moved = NavigationUtil.teleportCentered(player, target.x, y + 1, target.z);
                            break;
                        }
                    }
                }
            }

            if (moved) {
                player.sendTranslatable(NavigationMessages.THRU_MOVED);
            } else {
                player.sendTranslatable(NavigationMessages.THRU_OBSTRUCTED);
            }
            return context.success();
        }, SenderType.ACTUAL_PLAYER);
    }
}

