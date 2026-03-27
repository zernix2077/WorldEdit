package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.command.tree.CommandContext;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.server.command.tree.node.BaseNode;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;

import java.util.LinkedHashMap;

/**
 * Command node that parses {@link BlockPattern}.
 */
public final class BlockPatternNode extends BaseNode {
    public BlockPatternNode(String name, CommandNode parent) {
        super(name, parent, null);
    }

    @Override
    public boolean match(CommandContext context) {
        String arg = context.queryArg();
        try {
            BlockPattern pattern = BlockPattern.parse(arg);
            context.putResult(pattern);
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

