package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.command.tree.CommandContext;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.server.command.tree.node.BaseNode;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumConstraint;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;
import xyz.zernix.worldedit.message.ArgumentMessages;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command node that parses one or more trailing compact flags, e.g. {@code -h -v}.
 */
public final class FlagsNode<E extends Enum<E> & Flag> extends BaseNode {
    private final Class<E> enumType;
    private final List<E> flags;
    private final Map<Character, E> byToken;
    private final String allowedTokens;
    private final String enumName;

    public FlagsNode(String name, CommandNode parent, Class<E> enumType) {
        super(name, parent, null);
        this.enumType = enumType;

        E[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new IllegalArgumentException("Flags enum must contain at least one constant");
        }
        this.flags = List.of(constants);

        var map = new LinkedHashMap<Character, E>();
        for (E flag : flags) {
            char token = Character.toLowerCase(flag.token());
            if (map.put(token, flag) != null) {
                throw new IllegalArgumentException("Duplicate flag token: -" + token);
            }
        }
        this.byToken = Collections.unmodifiableMap(map);
        this.allowedTokens = flags.stream()
                .map(flag -> "-" + Character.toLowerCase(flag.token()))
                .collect(Collectors.joining(", "));
        this.enumName = flags.stream()
                .map(flag -> "-" + Character.toLowerCase(flag.token()))
                .collect(Collectors.joining("|"));
    }

    @Override
    public boolean match(CommandContext context) {
        if (!context.haveUnhandledArg()) {
            return false;
        }

        Set<E> parsed = EnumSet.noneOf(enumType);
        int consumed = 0;

        while (context.haveUnhandledArg() && isFlagToken(context.queryArg())) {
            String token = context.popArg();
            consumed++;
            String compact = token.substring(1);
            if (compact.isEmpty()) {
                context.addError(ArgumentMessages.ERR_FLAG_EMPTY_TOKEN);
                return false;
            }

            for (int i = 0; i < compact.length(); i++) {
                char symbol = Character.toLowerCase(compact.charAt(i));
                E flag = byToken.get(symbol);
                if (flag == null) {
                    context.addError(ArgumentMessages.ERR_FLAG_UNKNOWN, "-" + symbol, allowedTokens);
                    return false;
                }
                parsed.add(flag);
            }
        }

        if (consumed == 0) {
            return false;
        }

        context.putResult(Collections.unmodifiableSet(parsed));
        return true;
    }

    @Override
    public int getMaxArgCost() {
        return Short.MAX_VALUE;
    }

    @Override
    public CommandNode addLeaf(CommandNode leaf) {
        throw new UnsupportedOperationException("FlagsNode cannot add leaf nodes");
    }

    @Override
    public CommandParamData toNetworkData() {
        Set<CommandEnumConstraint> constraints = Collections.emptySet();
        var values = new LinkedHashMap<String, Set<CommandEnumConstraint>>();
        for (E flag : flags) {
            values.put("-" + Character.toLowerCase(flag.token()), constraints);
        }

        CommandParamData data = super.toNetworkData();
        data.setEnumData(new CommandEnumData(enumName, values, false));
        return data;
    }

    private static boolean isFlagToken(String token) {
        return token != null && token.length() >= 2 && token.charAt(0) == '-';
    }
}

