package xyz.zernix.worldedit.command.region;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import org.joml.Vector3i;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.RegionCommand;
import xyz.zernix.worldedit.command.arg.Direction;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Stacks selected region multiple times in a direction.
 */
public final class StackCommand extends RegionCommand {
    public StackCommand() {
        super("stack", "worldedit:command.stack.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
                .remain("args")
                .optional()
                .exec((context, player) -> {
                    @SuppressWarnings("unchecked")
                    List<String> args = context.getResult(0);
                    Parsed parsed = parse(args);
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
                        int copies = Math.max(1, parsed.count());
                        Direction direction = Direction.resolve(player, parsed.direction());
                        Vector3i unit = Direction.unit(direction);
                        int sx = bounds.maxX() - bounds.minX() + 1;
                        int sy = bounds.maxY() - bounds.minY() + 1;
                        int sz = bounds.maxZ() - bounds.minZ() + 1;

                        Vector3i step;
                        if (unit.x != 0) {
                            step = new Vector3i(unit).mul(sx);
                        } else if (unit.y != 0) {
                            step = new Vector3i(unit).mul(sy);
                        } else {
                            step = new Vector3i(unit).mul(sz);
                        }

                        List<SourceBlock> source = new ArrayList<>();
                        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                                    source.add(new SourceBlock(x, y, z, dimension.getBlockState(x, y, z)));
                                }
                            }
                        }

                        int total = 0;
                        for (int i = 1; i <= copies; i++) {
                            int dx = step.x * i;
                            int dy = step.y * i;
                            int dz = step.z * i;
                            for (SourceBlock block : source) {
                                int tx = block.x() + dx;
                                int ty = block.y() + dy;
                                int tz = block.z() + dz;
                                if (dimension.isInWorld(tx, ty, tz)) {
                                    snapshot.setBlockState(tx, ty, tz, block.state());
                                    total++;
                                }
                            }
                        }

                        player.sendTranslatable(RegionMessages.STACK_SUCCESS, copies, total);
                    });
                }, SenderType.ACTUAL_PLAYER);
    }

    private static Parsed parse(List<String> args) {
        if (args == null || args.isEmpty()) {
            return new Parsed(1, null);
        }
        if (args.size() == 1) {
            Integer count = tryParseInt(args.getFirst());
            if (count != null) {
                return new Parsed(Math.max(1, count), null);
            }
            Direction direction = Direction.parse(args.getFirst());
            if (direction != null) {
                return new Parsed(1, direction);
            }
            return null;
        }
        if (args.size() == 2) {
            Integer count = tryParseInt(args.get(0));
            Direction direction = Direction.parse(args.get(1));
            if (count == null || direction == null) {
                return null;
            }
            return new Parsed(Math.max(1, count), direction);
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

    private record Parsed(int count, Direction direction) {
    }

    private record SourceBlock(int x, int y, int z, BlockState state) {
    }
}

