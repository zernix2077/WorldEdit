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
 * Shrinks the selection equally from all faces.
 */
public final class InsetCommand extends WorldEditCommand {
    private enum InsetFlag implements Flag {
        HORIZONTAL('h'),
        VERTICAL('v');

        private final char token;

        InsetFlag(char token) {
            this.token = token;
        }

        @Override
        public char token() {
            return token;
        }
    }

    public InsetCommand() {
        super("inset", "worldedit:command.inset.description");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        var amount = tree.getRoot().intNum("amount");
        amount.exec((context, player) -> applyInset(context, player, context.getResult(0), Set.of()), SenderType.ACTUAL_PLAYER);
        amount.addLeaf(parent -> new FlagsNode<>("flags", parent, InsetFlag.class))
                .exec((context, player) -> applyInset(context, player, context.getResult(0), context.getResult(1)), SenderType.ACTUAL_PLAYER);
    }

    private static org.allaymc.api.command.CommandResult applyInset(
            org.allaymc.api.command.tree.CommandContext context,
            org.allaymc.api.entity.interfaces.EntityPlayer player,
            int rawAmount,
            Set<InsetFlag> flags
    ) {
        Selection.Bounds bounds = Selection.bounds(player);
        if (bounds == null) {
            return context.success();
        }

        int amount = Math.max(0, rawAmount);
        boolean horizontal = flags.contains(InsetFlag.HORIZONTAL);
        boolean vertical = flags.contains(InsetFlag.VERTICAL);
        boolean contractHorizontal = !vertical || horizontal;
        boolean contractVertical = !horizontal || vertical;
        int ax = contractHorizontal ? amount : 0;
        int ay = contractVertical ? amount : 0;
        int az = contractHorizontal ? amount : 0;

        Selection.Bounds out = new Selection.Bounds(
                bounds.minX() + ax,
                bounds.maxX() - ax,
                bounds.minY() + ay,
                bounds.maxY() - ay,
                bounds.minZ() + az,
                bounds.maxZ() - az
        );
        if (out.isValid() && Selection.applyBounds(player, out)) {
            player.sendTranslatable(SelectionMessages.INSET);
        } else {
            player.sendTranslatable(SelectionMessages.EXPAND_OBSTRUCTED);
        }
        return context.success();
    }
}

