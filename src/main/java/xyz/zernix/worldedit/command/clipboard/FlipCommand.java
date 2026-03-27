package xyz.zernix.worldedit.command.clipboard;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Clipboard;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.command.arg.Direction;
import xyz.zernix.worldedit.command.arg.DirectionNode;
import xyz.zernix.worldedit.message.ClipboardMessages;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.List;
import java.util.Locale;

/**
 * Mirrors clipboard by axis.
 */
public final class FlipCommand extends WorldEditCommand {
    public FlipCommand() {
        super("flip", "worldedit:command.flip.description", List.of("mirror"));
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .addLeaf(parent -> new DirectionNode("direction", parent))
                .optional()
                .exec((context, player) -> {
                    Clipboard.Entry entry = Clipboard.get(player);
                    if (entry == null) {
                        player.sendTranslatable(RegionMessages.ERR_CLIPBOARD_EMPTY);
                        return context.success();
                    }

                    Direction direction = Direction.resolve(player, context.getResult(0));
                    Clipboard.transform(player, (x, y, z, origin) -> {
                        int rx = x - origin.x();
                        int ry = y - origin.y();
                        int rz = z - origin.z();

                        int fx = rx;
                        int fy = ry;
                        int fz = rz;
                        switch (direction) {
                            case NORTH, SOUTH -> fz = -rz;
                            case EAST, WEST -> fx = -rx;
                            case UP, DOWN -> fy = -ry;
                            case ME -> {
                            }
                        }

                        return new Vector3i(origin.x() + fx, origin.y() + fy, origin.z() + fz);
                    });

                    player.sendTranslatable(ClipboardMessages.FLIPPED, direction.name().toLowerCase(Locale.ROOT), entry.snapshot().size());
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }
}

