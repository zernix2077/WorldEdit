package xyz.zernix.worldedit.command.clipboard;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Clipboard;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.message.ClipboardMessages;
import xyz.zernix.worldedit.message.RegionMessages;

/**
 * Rotates clipboard around stored origin using 90-degree steps.
 */
public final class RotateCommand extends WorldEditCommand {
    public RotateCommand() {
        super("rotate", "worldedit:command.rotate.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("rotateY")
                .intNum("rotateX")
                .optional()
                .intNum("rotateZ")
                .optional()
                .exec((context, player) -> {
                    Clipboard.Entry entry = Clipboard.get(player);
                    if (entry == null) {
                        player.sendTranslatable(RegionMessages.ERR_CLIPBOARD_EMPTY);
                        return context.success();
                    }

                    int rotateY = context.getResult(0);
                    int rotateX = context.getResult(1);
                    int rotateZ = context.getResult(2);

                    Integer yTurns = toQuarterTurns(rotateY);
                    if (yTurns == null) {
                        player.sendTranslatable(ClipboardMessages.ERR_ROTATE_INVALID, "Y", rotateY);
                        return context.success();
                    }
                    Integer xTurns = toQuarterTurns(rotateX);
                    if (xTurns == null) {
                        player.sendTranslatable(ClipboardMessages.ERR_ROTATE_INVALID, "X", rotateX);
                        return context.success();
                    }
                    Integer zTurns = toQuarterTurns(rotateZ);
                    if (zTurns == null) {
                        player.sendTranslatable(ClipboardMessages.ERR_ROTATE_INVALID, "Z", rotateZ);
                        return context.success();
                    }

                    Clipboard.transform(player, (x, y, z, origin) -> {
                        int tx = x - origin.x();
                        int ty = y - origin.y();
                        int tz = z - origin.z();

                        for (int i = 0; i < yTurns; i++) {
                            int nx = tz;
                            int ny = ty;
                            int nz = -tx;
                            tx = nx;
                            ty = ny;
                            tz = nz;
                        }

                        for (int i = 0; i < xTurns; i++) {
                            int nx = tx;
                            int ny = -tz;
                            int nz = ty;
                            tx = nx;
                            ty = ny;
                            tz = nz;
                        }

                        for (int i = 0; i < zTurns; i++) {
                            int nx = -ty;
                            int ny = tx;
                            int nz = tz;
                            tx = nx;
                            ty = ny;
                            tz = nz;
                        }

                        return new Vector3i(origin.x() + tx, origin.y() + ty, origin.z() + tz);
                    });

                    player.sendTranslatable(ClipboardMessages.ROTATED, entry.snapshot().size());
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }

    private static Integer toQuarterTurns(int degrees) {
        if (degrees % 90 != 0) {
            return null;
        }
        int normalized = degrees % 360;
        if (normalized < 0) {
            normalized += 360;
        }
        return normalized / 90;
    }
}

