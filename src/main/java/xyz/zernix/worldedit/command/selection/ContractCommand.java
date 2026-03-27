package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.command.arg.Direction;
import xyz.zernix.worldedit.message.SelectionMessages;

import java.util.List;

/**
 * Contracts the current selection along a direction, optionally on both sides.
 */
public final class ContractCommand extends WorldEditCommand {
    public ContractCommand() {
        super("contract", "worldedit:command.contract.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .intNum("amount")
                .remain("tail")
                .optional()
                .exec((context, player) -> {
                    Selection.Bounds bounds = Selection.bounds(player);
                    if (bounds == null) {
                        return context.success();
                    }

                    int amount = context.getResult(0);
                    @SuppressWarnings("unchecked")
                    List<String> tail = context.getResult(1);

                    Parsed parsed = parseTail(tail, player);
                    if (parsed == null) {
                        return context.fail();
                    }

                    Vector3i unit = Direction.unit(parsed.direction);
                    Vector3i forward = new Vector3i(unit).mul(-amount);
                    Vector3i reverse = new Vector3i(unit).mul(parsed.reverseAmount);
                    Selection.Bounds out = applyDirectionalResize(applyDirectionalResize(bounds, forward), reverse);
                    if (out.isValid() && Selection.applyBounds(player, out)) {
                        player.sendTranslatable(SelectionMessages.CONTRACTED, bounds.volume() - out.volume());
                    } else {
                        player.sendTranslatable(SelectionMessages.EXPAND_OBSTRUCTED);
                    }
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }

    private static Selection.Bounds applyDirectionalResize(Selection.Bounds bounds, Vector3i delta) {
        int minX = bounds.minX();
        int maxX = bounds.maxX();
        int minY = bounds.minY();
        int maxY = bounds.maxY();
        int minZ = bounds.minZ();
        int maxZ = bounds.maxZ();

        if (delta.x > 0) {
            maxX += delta.x;
        } else if (delta.x < 0) {
            minX += delta.x;
        }

        if (delta.y > 0) {
            maxY += delta.y;
        } else if (delta.y < 0) {
            minY += delta.y;
        }

        if (delta.z > 0) {
            maxZ += delta.z;
        } else if (delta.z < 0) {
            minZ += delta.z;
        }

        return new Selection.Bounds(minX, maxX, minY, maxY, minZ, maxZ);
    }

    private static Parsed parseTail(List<String> tail, org.allaymc.api.entity.interfaces.EntityPlayer player) {
        if (tail == null || tail.isEmpty()) {
            return new Parsed(0, Direction.fromAim(player));
        }
        if (tail.size() == 1) {
            Integer value = tryParseInt(tail.getFirst());
            if (value != null) {
                return new Parsed(Math.max(0, value), Direction.fromAim(player));
            }
            Direction direction = Direction.parse(tail.getFirst());
            if (direction == null) {
                player.sendTranslatable(SelectionMessages.ERR_DIRECTION_INVALID, tail.getFirst());
                return null;
            }
            return new Parsed(0, Direction.resolve(player, direction));
        }
        if (tail.size() == 2) {
            Integer value = tryParseInt(tail.get(0));
            Direction direction = Direction.parse(tail.get(1));
            if (value == null || direction == null) {
                if (direction == null) {
                    player.sendTranslatable(SelectionMessages.ERR_DIRECTION_INVALID, tail.get(1));
                }
                return null;
            }
            return new Parsed(Math.max(0, value), Direction.resolve(player, direction));
        }
        return null;
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record Parsed(int reverseAmount, Direction direction) {
    }
}

