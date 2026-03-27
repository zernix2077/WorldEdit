package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.command.tree.CommandContext;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.server.command.tree.node.BaseNode;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumConstraint;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;
import xyz.zernix.worldedit.message.SelectionMessages;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Custom command node that parses {@link Direction}.
 */
public final class DirectionNode extends BaseNode {
    public DirectionNode(String name, CommandNode parent) {
        super(name, parent, null);
    }

    @Override
    public boolean match(CommandContext context) {
        String arg = context.queryArg();
        Direction direction = Direction.parse(arg);
        if (direction == null) {
            context.addError(SelectionMessages.ERR_DIRECTION_INVALID, arg);
            return false;
        }

        context.putResult(direction);
        context.popArg();
        return true;
    }

    @Override
    public CommandParamData toNetworkData() {
        Set<CommandEnumConstraint> constraints = Collections.emptySet();
        Map<String, Set<CommandEnumConstraint>> values = new LinkedHashMap<>();
        for (String name : new String[]{"north", "n", "south", "s", "east", "e", "west", "w", "up", "u", "down", "d", "me", "m"}) {
            values.put(name, constraints);
        }

        CommandParamData data = super.toNetworkData();
        data.setEnumData(new CommandEnumData("Direction", values, false));
        return data;
    }
}

