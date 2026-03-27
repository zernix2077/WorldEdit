package xyz.zernix.worldedit.command.arg;

import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.joml.Vector3i;

import java.util.Locale;

/**
 * Direction argument used by movement and selection commands.
 */
public enum Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    UP,
    DOWN,
    ME;

    /**
     * Resolves direction from player look vector.
     */
    public static Direction fromAim(EntityPlayer player) {
        var location = player.getLocation();
        if (location.pitch() > 67.5d) {
            return DOWN;
        }
        if (location.pitch() < -67.5d) {
            return UP;
        }

        double rotation = ((location.yaw() % 360d) + 360d) % 360d;
        if (rotation >= 45d && rotation < 135d) {
            return WEST;
        }
        if (rotation >= 135d && rotation < 225d) {
            return NORTH;
        }
        if (rotation >= 225d && rotation < 315d) {
            return EAST;
        }
        return SOUTH;
    }

    /**
     * Resolves effective direction, where {@link #ME} and {@code null} use look direction.
     */
    public static Direction resolve(EntityPlayer player, Direction direction) {
        if (direction == null || direction == ME) {
            return fromAim(player);
        }
        return direction;
    }

    /**
     * Returns unit vector for a direction.
     */
    public static Vector3i unit(Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3i(0, 0, -1);
            case SOUTH -> new Vector3i(0, 0, 1);
            case EAST -> new Vector3i(1, 0, 0);
            case WEST -> new Vector3i(-1, 0, 0);
            case UP -> new Vector3i(0, 1, 0);
            case DOWN -> new Vector3i(0, -1, 0);
            case ME -> new Vector3i(0, 0, 0);
        };
    }

    /**
     * Parses direction from command token.
     *
     * @return parsed direction or {@code null} when token is invalid.
     */
    public static Direction parse(String raw) {
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "north", "n" -> NORTH;
            case "south", "s" -> SOUTH;
            case "east", "e" -> EAST;
            case "west", "w" -> WEST;
            case "up", "u" -> UP;
            case "down", "d" -> DOWN;
            case "me", "m" -> ME;
            default -> null;
        };
    }
}

