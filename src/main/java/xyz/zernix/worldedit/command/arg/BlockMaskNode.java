package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.command.tree.CommandContext;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.server.command.tree.node.BaseNode;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;

import java.util.LinkedHashMap;

/**
 * Command node that parses {@link BlockMask}.
 */
public final class BlockMaskNode extends BaseNode {
    public BlockMaskNode(String name, CommandNode parent) {
        super(name, parent, null);
    }

    @Override
    public boolean match(CommandContext context) {
        String arg = context.queryArg();
        try {
            BlockMask mask = BlockMask.parse(arg);
            context.putResult(mask);
            context.popArg();
            return true;
        } catch (ArgumentParseException exception) {
            context.addError(exception.key(), exception.args());
            return false;
        }
    }

    @Override
    public CommandParamData toNetworkData() {
        CommandParamData data = super.toNetworkData();
        data.setEnumData(new CommandEnumData("Block", new LinkedHashMap<>(), false));
        return data;
    }
}

