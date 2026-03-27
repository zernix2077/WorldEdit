package xyz.zernix.worldedit;

import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player clipboard storing a single snapshot outside undo/redo history.
 *
 * <p>Clipboard remembers copy origin to support relative pasting.</p>
 */
public final class Clipboard {
    private static final Map<UUID, Entry> STATE = new ConcurrentHashMap<>();

    private Clipboard() {
    }

    /**
     * Clipboard entry consisting of snapshot and copy origin.
     */
    public record Entry(Snapshot snapshot, Vector3ic origin) {
    }

    /**
     * Sets clipboard entry for a player.
     */
    public static void set(EntityPlayer player, Snapshot snapshot, Vector3ic origin) {
        STATE.put(player.getUniqueId(), new Entry(snapshot, new Vector3i(origin)));
    }

    /**
     * Gets clipboard entry for a player or {@code null} if empty.
     */
    public static Entry get(EntityPlayer player) {
        return STATE.get(player.getUniqueId());
    }

    /**
     * Clears player's clipboard.
     *
     * @return {@code true} if clipboard previously had a value.
     */
    public static boolean clear(EntityPlayer player) {
        return STATE.remove(player.getUniqueId()) != null;
    }

    /**
     * Transforms stored clipboard coordinates around the stored origin.
     *
     * @return {@code true} if clipboard existed and was transformed.
     */
    public static boolean transform(EntityPlayer player, CoordinateMapper mapper) {
        Entry entry = get(player);
        if (entry == null) {
            return false;
        }

        Snapshot transformed = Snapshot.take(entry.snapshot().dimension(), out -> entry.snapshot().forEachAfter((x, y, z, block) -> {
            Vector3ic coordinates = mapper.map(x, y, z, entry.origin());
            out.record(coordinates.x(), coordinates.y(), coordinates.z(), block);
        }));

        set(player, transformed, entry.origin());
        return true;
    }

    /**
     * Clears clipboard for a player.
     */
    public static void clearState(EntityPlayer player) {
        STATE.remove(player.getUniqueId());
    }

    /**
     * Mapping callback for coordinate transformation.
     */
    @FunctionalInterface
    public interface CoordinateMapper {
        Vector3ic map(int x, int y, int z, Vector3ic origin);
    }
}

