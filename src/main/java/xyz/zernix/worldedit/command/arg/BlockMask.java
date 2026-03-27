package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.block.property.type.BlockPropertyType;
import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockType;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.utils.identifier.Identifier;
import xyz.zernix.worldedit.message.SelectionMessages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Predicate-based block mask.
 */
public final class BlockMask {
    private final List<Predicate<BlockState>> checks;

    private BlockMask(List<Predicate<BlockState>> checks) {
        this.checks = List.copyOf(checks);
    }

    /**
     * Checks if block state passes mask.
     */
    public boolean test(BlockState state) {
        for (Predicate<BlockState> check : checks) {
            if (check.test(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses mask specification.
     */
    public static BlockMask parse(String spec) throws ArgumentParseException {
        List<String> tokens = tokenize(spec);
        if (tokens.isEmpty()) {
            throw new ArgumentParseException(SelectionMessages.ERR_MASK_NO_BLOCKS_SPECIFIED);
        }

        List<Predicate<BlockState>> checks = new ArrayList<>();
        for (String token : tokens) {
            checks.add(parseSingle(token));
        }
        return new BlockMask(checks);
    }

    private static List<String> tokenize(String raw) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '[') {
                depth++;
                current.append(ch);
            } else if (ch == ']') {
                depth--;
                current.append(ch);
            } else if (ch == ',' && depth == 0) {
                String token = current.toString().trim();
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        String last = current.toString().trim();
        if (!last.isEmpty()) {
            tokens.add(last);
        }
        return tokens;
    }

    private static Predicate<BlockState> parseSingle(String tokenRaw) throws ArgumentParseException {
        String token = tokenRaw.trim();
        boolean negated = token.startsWith("!");
        String body = negated ? token.substring(1).trim() : token;

        int propsStart = body.indexOf('[');
        String idPart = propsStart < 0 ? body : body.substring(0, propsStart);
        String propsPart = propsStart < 0 ? "" : body.substring(propsStart);
        String fullId = idPart.contains(":") ? idPart : Identifier.DEFAULT_NAMESPACE + ":" + idPart;

        BlockType<?> blockType = Registries.BLOCKS.get(new Identifier(fullId));
        if (blockType == null) {
            throw new ArgumentParseException(SelectionMessages.ERR_MASK_BLOCK_ID_INVALID, fullId);
        }

        List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> properties = parseProperties(blockType, propsPart);
        BlockState exactState = properties.isEmpty() ? null : blockType.ofState(properties);
        if (!properties.isEmpty() && exactState == null) {
            throw new ArgumentParseException(SelectionMessages.ERR_MASK_BLOCK_PROPS_INVALID_STATE, fullId + ":" + propsPart);
        }

        Predicate<BlockState> predicate;
        if (exactState == null) {
            predicate = state -> state.getBlockType() == blockType;
        } else {
            predicate = state -> state == exactState;
        }

        if (negated) {
            return state -> !predicate.test(state);
        }
        return predicate;
    }

    private static List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> parseProperties(BlockType<?> blockType, String raw) throws ArgumentParseException {
        if (raw.isEmpty()) {
            return List.of();
        }
        if (!raw.startsWith("[") || !raw.endsWith("]")) {
            throw new ArgumentParseException(SelectionMessages.ERR_MASK_BLOCK_PROPS_INVALID_SYNTAX, raw);
        }

        String inside = raw.substring(1, raw.length() - 1);
        String[] pairs = inside.split(",");
        Map<BlockPropertyType<?>, BlockPropertyType.BlockPropertyValue<?, ?, ?>> values = new LinkedHashMap<>();
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] keyValue = trimmed.split("=", 2);
            String key = removeQuotes(keyValue[0].trim());
            String value = keyValue.length == 2 ? removeQuotes(keyValue[1].trim()) : "";

            BlockPropertyType<?> propertyType = blockType.getProperties().get(key);
            if (propertyType == null) {
                throw new ArgumentParseException(SelectionMessages.ERR_MASK_BLOCK_PROP_MISSING, key);
            }

            try {
                values.put(propertyType, propertyType.tryCreateValue(value));
            } catch (IllegalArgumentException exception) {
                throw new ArgumentParseException(SelectionMessages.ERR_MASK_BLOCK_PROP_TYPE_MISMATCH, key, value);
            }
        }

        for (BlockPropertyType.BlockPropertyValue<?, ?, ?> defaultValue : blockType.getDefaultState().getPropertyValues().values()) {
            values.putIfAbsent(defaultValue.getPropertyType(), defaultValue);
        }

        return new ArrayList<>(values.values());
    }

    private static String removeQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}

