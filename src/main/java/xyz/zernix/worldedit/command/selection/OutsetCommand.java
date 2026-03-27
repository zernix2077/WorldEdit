package xyz.zernix.worldedit.command.selection;

import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandTree;
import xyz.zernix.worldedit.Selection;
import xyz.zernix.worldedit.command.WorldEditCommand;
import xyz.zernix.worldedit.command.arg.Flag;
import xyz.zernix.worldedit.command.arg.FlagsNode;
import xyz.zernix.worldedit.message.SelectionMessages;

import java.util.Set;

/**
 * Grows the selection equally in all directions.
 */
public final class OutsetCommand extends WorldEditCommand {
    private enum OutsetFlag implements Flag {
        HORIZONTAL('h'),
        VERTICAL('v');

        private final char token;

        OutsetFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public OutsetCommand() {
        super("outset", "worldedit:command.outset.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var amount = tree.getRoot().intNum("amount");
        amount.exec((context, player) -> applyOutset(context, player, context.getResult(0), Set.of()), SenderType.ACTUAL_PLAYER);
        amount.addLeaf(parent -> new FlagsNode<>("flags", parent, OutsetFlag.class))
                .exec((context, player) -> applyOutset(context, player, context.getResult(0), context.getResult(1)), SenderType.ACTUAL_PLAYER);
    }

    private static org.allaymc.api.command.CommandResult applyOutset(
            org.allaymc.api.command.tree.CommandContext context,
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            int rawAmount,
            Set<OutsetFlag> flags
    ) {
        Selection.Bounds bounds = Selection.bounds(player);
        if (bounds == null) {
            return context.success();
        }

        int amount = Math.max(0, rawAmount);
        boolean horizontal = flags.contains(OutsetFlag.HORIZONTAL);
        boolean vertical = flags.contains(OutsetFlag.VERTICAL);
        boolean expandHorizontal = !vertical || horizontal;
        boolean expandVertical = !horizontal || vertical;
        int ax = expandHorizontal ? amount : 0;
        int ay = expandVertical ? amount : 0;
        int az = expandHorizontal ? amount : 0;

        Selection.Bounds out = new Selection.Bounds(
                bounds.minX() - ax,
                bounds.maxX() + ax,
                bounds.minY() - ay,
                bounds.maxY() + ay,
                bounds.minZ() - az,
                bounds.maxZ() + az
        );
        if (Selection.applyBounds(player, out)) {
            player.sendTranslatable(SelectionMessages.OUTSET);
        }
        return context.success();
    }
}

