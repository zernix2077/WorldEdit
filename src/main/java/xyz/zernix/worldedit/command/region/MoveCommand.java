package xyz.zernix.worldedit.command.region;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.ArgumentParseException;
import xyz.zernix.worldedit.command.arg.BlockPattern;
import xyz.zernix.worldedit.command.arg.Direction;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Moves selected region along a direction and fills source with replacement.
 */
public final class MoveCommand extends RegionCommand {
    public MoveCommand() {
        super("move", "worldedit:command.move.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .remain("args")
                .optional()
                .exec((context, player) -> {
                    @SuppressWarnings("unchecked")
                    List<String> args = context.getResult(0);
                    Parsed parsed = parse(args, player);
                    if (parsed == null) {
                        context.addSyntaxError();
                        return context.fail();
                    }

                    return runWithSnapshot(context, player, snapshot -> {
                        Selection.Bounds bounds = Selection.bounds(player);
                        if (bounds == null) {
                            return;
                        }

                        var dimension = player.getLocation().dimension();
                        int multiplier = Math.max(1, parsed.multiplier());
                        Direction direction = Direction.resolve(player, parsed.direction());
                        Vector3i delta = Direction.unit(direction).mul(multiplier);
                        BlockPattern fill = parsed.replace();
                        BlockState air = BlockTypes.AIR.getDefaultState();

                        List<SourceBlock> source = new ArrayList<>();
                        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                    source.add(new SourceBlock(x, y, z, dimension.getBlockState(x, y, z)));
                                }
                            }
                        }

                        for (SourceBlock block : source) {
                            snapshot.setBlockState(block.x(), block.y(), block.z(), fill == null ? air : fill.next());
                        }

                        int total = 0;
                        for (SourceBlock block : source) {
                            int tx = block.x() + delta.x;
                            int ty = block.y() + delta.y;
                            int tz = block.z() + delta.z;
                            if (dimension.isInWorld(tx, ty, tz)) {
                                snapshot.setBlockState(tx, ty, tz, block.state());
                                total++;
                            }
                        }

                        Selection.Bounds shifted = bounds.shifted(delta.x, delta.y, delta.z);
                        if (shifted.isValid()) {
                            Selection.applyBounds(player, shifted);
                        }

                        player.sendTranslatable(RegionMessages.MOVE_SUCCESS, total, delta.x, delta.y, delta.z);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }

    private Parsed parse(List<String> args, org.allaymc.api.entity.interfaces.EntityPlayer player) {
        if (args == null || args.isEmpty()) {
            return new Parsed(1, null, null);
        }

        int index = 0;
        int multiplier = 1;
        Integer value = tryParseInt(args.get(index));
        if (value != null) {
            multiplier = Math.max(1, value);
            index++;
        }

        Direction direction = null;
        if (index < args.size()) {
            Direction parsedDirection = Direction.parse(args.get(index));
            if (parsedDirection != null) {
                direction = parsedDirection;
                index++;
            }
        }

        BlockPattern replace = null;
        if (index < args.size()) {
            if (index != args.size() - 1) {
                return null;
            }
            try {
                replace = BlockPattern.parse(args.get(index));
            } catch (ArgumentParseException exception) {
                player.sendTranslatable(exception.key(), exception.args());
                return null;
            }
        }

        return new Parsed(multiplier, direction, replace);
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record Parsed(int multiplier, Direction direction, BlockPattern replace) {
    }

    private record SourceBlock(int x, int y, int z, BlockState state) {
    }
}

