package xyz.zernix.worldedit.command.region;

import xyz.zernix.worldedit.command.arg.ArgumentParseException;
import xyz.zernix.worldedit.message.RegionMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsers for shape radii arguments.
 */
final class ShapeArgs {
    private ShapeArgs() {
    }

    static XZ parseXZ(String raw) throws ArgumentParseException {
        List<Integer> values = parsePositive(raw);
        if (values.size() == 1) {
            int r = values.getFirst();
            return new XZ(r, r);
        }
        if (values.size() == 2) {
            return new XZ(values.get(0), values.get(1));
        }
        throw new ArgumentParseException(RegionMessages.ERR_RADII_INVALID, raw, "r or r1,r2");
    }

    static XYZ parseXYZ(String raw) throws ArgumentParseException {
        List<Integer> values = parsePositive(raw);
        if (values.size() == 1) {
            int r = values.getFirst();
            return new XYZ(r, r, r);
        }
        if (values.size() == 3) {
            return new XYZ(values.get(0), values.get(1), values.get(2));
        }
        throw new ArgumentParseException(RegionMessages.ERR_RADII_INVALID, raw, "r or r1,r2,r3");
    }

    private static List<Integer> parsePositive(String raw) throws ArgumentParseException {
        String[] parts = raw.split("[,xX]");
        List<Integer> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                int value = Integer.parseInt(trimmed);
                if (value <= 0) {
                    throw new ArgumentParseException(RegionMessages.ERR_RADII_INVALID, raw, "positive integers");
                }
                values.add(value);
            } catch (NumberFormatException exception) {
                throw new ArgumentParseException(RegionMessages.ERR_RADII_INVALID, raw, "positive integers");
            }
        }
        if (values.isEmpty()) {
            throw new ArgumentParseException(RegionMessages.ERR_RADII_INVALID, raw, "positive integers");
        }
        return values;
    }

    record XZ(int rx, int rz) {
    }

    record XYZ(int rx, int ry, int rz) {
    }
}

