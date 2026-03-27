package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.block.property.type.BlockPropertyType;
import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockType;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.utils.identifier.Identifier;
import xyz.zernix.worldedit.message.PatternMessages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weighted block pattern.
 */
public final class BlockPattern {
    private final List<Entry> entries;
    private final int[] cumulativeWeights;
    private final int totalWeight;

    private BlockPattern(List<Entry> entries) {
        this.entries = List.copyOf(entries);
        this.cumulativeWeights = new int[entries.size()];

        int sum = 0;
        for (int i = 0; i < entries.size(); i++) {
            sum += entries.get(i).weight();
            cumulativeWeights[i] = sum;
        }
        this.totalWeight = sum;
    }

    /**
     * Returns next random block state according to configured weights.
     */
    public BlockState next() {
        int n = ThreadLocalRandom.current().nextInt(totalWeight) + 1;
        int lo = 0;
        int hi = cumulativeWeights.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cumulativeWeights[mid] < n) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return entries.get(lo).state();
    }

    /**
     * Parses block pattern specification.
     */
    public static BlockPattern parse(String spec) throws ArgumentParseException {
        List<String> tokens = tokenize(spec);
        List<Entry> entries = new ArrayList<>();
        for (String token : tokens) {
            entries.add(parseSingle(token));
        }

        if (entries.isEmpty()) {
            throw new ArgumentParseException(PatternMessages.ERR_NO_BLOCKS_SPECIFIED);
        }
        return new BlockPattern(entries);
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
                if (!current.isEmpty()) {
                    tokens.add(current.toString().trim());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString().trim());
        }
        return tokens.stream().filter(s -> !s.isEmpty()).toList();
    }

    private static Entry parseSingle(String token) throws ArgumentParseException {
        String[] parts = token.split("%", 2);
        String weightStr;
        String remainder;
        if (parts.length == 2) {
            weightStr = parts[0];
            remainder = parts[1];
        } else {
            weightStr = "";
            remainder = parts[0];
        }

        int weight = 1;
        if (!weightStr.isBlank()) {
            try {
                weight = Integer.parseInt(weightStr);
            } catch (NumberFormatException exception) {
                throw new ArgumentParseException(PatternMessages.ERR_WEIGHT_INVALID, weightStr);
            }
            if (weight <= 0) {
                throw new ArgumentParseException(PatternMessages.ERR_WEIGHT_INVALID, weightStr);
            }
        }

        int propsStart = remainder.indexOf('[');
        String idPart = propsStart < 0 ? remainder : remainder.substring(0, propsStart);
        String propsPart = propsStart < 0 ? "" : remainder.substring(propsStart);
        String fullId = idPart.contains(":") ? idPart : Identifier.DEFAULT_NAMESPACE + ":" + idPart;

        BlockType<?> blockType = Registries.BLOCKS.get(new Identifier(fullId));
        if (blockType == null) {
            throw new ArgumentParseException(PatternMessages.ERR_BLOCK_ID_INVALID, fullId);
        }

        List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> properties = parseProperties(blockType, propsPart);
        BlockState state = properties.isEmpty() ? blockType.getDefaultState() : blockType.ofState(properties);
        if (state == null) {
            throw new ArgumentParseException(PatternMessages.ERR_BLOCK_PROPERTIES_INVALID_STATE, fullId + ":" + propsPart);
        }

        return new Entry(state, weight);
    }

    private static List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> parseProperties(BlockType<?> blockType, String raw) throws ArgumentParseException {
        if (raw.isEmpty()) {
            return List.of();
        }
        if (!raw.startsWith("[") || !raw.endsWith("]")) {
            throw new ArgumentParseException(PatternMessages.ERR_BLOCK_PROPERTIES_INVALID_SYNTAX, raw);
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
                throw new ArgumentParseException(PatternMessages.ERR_BLOCK_PROPERTY_MISSING, key);
            }

            try {
                values.put(propertyType, propertyType.tryCreateValue(value));
            } catch (IllegalArgumentException exception) {
                throw new ArgumentParseException(PatternMessages.ERR_BLOCK_PROPERTY_TYPE_MISMATCH, key, value);
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

    private record Entry(BlockState state, int weight) {
    }
}

