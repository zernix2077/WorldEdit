package xyz.zernix.worldedit;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.world.Dimension;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A single world-edit operation snapshot.
 *
 * <p>Each snapshot stores block states before and after mutation and can be used for undo/redo.</p>
 */
public final class Snapshot {
    private static final int MAX_HISTORY = 30;
    private static final Map<UUID, Timeline> HISTORY = new ConcurrentHashMap<>();

    private final Dimension dimension;
    private final LinkedHashMap<Vector3i, BlockState> before = new LinkedHashMap<>();
    private final LinkedHashMap<Vector3i, BlockState> after = new LinkedHashMap<>();
    private boolean sealed;

    private Snapshot(Dimension dimension) {
        this.dimension = dimension;
    }

    /**
     * Returns the dimension associated with this snapshot.
     */
    public Dimension dimension() {
        return dimension;
    }

    /**
     * Stores a resulting state without mutating the world.
     */
    public void record(int x, int y, int z, BlockState block) {
        ensureOpen();
        after.put(new Vector3i(x, y, z), block);
    }

    /**
     * Sets a block in the world and records the mutation.
     */
    public void setBlockState(int x, int y, int z, BlockState block) {
        ensureOpen();
        Vector3i pos = new Vector3i(x, y, z);
        BlockState original = before.computeIfAbsent(pos, ignored -> dimension.getBlockState(x, y, z));

        dimension.setBlockState(x, y, z, block);
        if (block == original) {
            after.remove(pos);
        } else {
            after.put(pos, block);
        }
    }

    /**
     * Returns number of effective changed blocks in this snapshot.
     */
    public int size() {
        return after.size();
    }

    /**
     * Iterates over final block states in this snapshot.
     */
    public void forEachAfter(AfterConsumer consumer) {
        for (Map.Entry<Vector3i, BlockState> entry : after.entrySet()) {
            Vector3i pos = entry.getKey();
            consumer.accept(pos.x, pos.y, pos.z, entry.getValue());
        }
    }

    private void ensureOpen() {
        if (sealed) {
            throw new IllegalStateException("Snapshot is sealed and cannot accept new changes");
        }
    }

    private void seal() {
        this.sealed = true;
    }

    private int undo() {
        return applyStates(before);
    }

    private int redo() {
        return applyStates(after);
    }

    private int applyStates(Map<Vector3i, BlockState> states) {
        int changed = 0;
        for (Map.Entry<Vector3i, BlockState> entry : states.entrySet()) {
            Vector3i pos = entry.getKey();
            dimension.setBlockState(pos.x, pos.y, pos.z, entry.getValue());
            changed++;
        }
        return changed;
    }

    /**
     * Builds a detached snapshot that is not added to undo/redo history.
     */
    public static Snapshot take(Dimension dimension, Consumer<Snapshot> action) {
        Snapshot snapshot = new Snapshot(dimension);
        action.accept(snapshot);
        snapshot.seal();
        return snapshot;
    }

    /**
     * Builds and appends a snapshot for this player.
     */
    public static RecordResult record(EntityPlayer player, Consumer<Snapshot> action) {
        Timeline timeline = timeline(player);
        synchronized (timeline) {
            if (timeline.busy) {
                return RecordResult.busyResult();
            }

            timeline.busy = true;
            try {
                Snapshot snapshot = new Snapshot(player.getLocation().dimension());
                action.accept(snapshot);
                snapshot.seal();

                if (snapshot.size() == 0) {
                    return RecordResult.done(0, false);
                }

                if (timeline.cursor < timeline.edits.size()) {
                    timeline.edits.subList(timeline.cursor, timeline.edits.size()).clear();
                }

                timeline.edits.add(snapshot);
                timeline.cursor = timeline.edits.size();

                int extra = timeline.edits.size() - MAX_HISTORY;
                if (extra > 0) {
                    timeline.edits.subList(0, extra).clear();
                    timeline.cursor = Math.max(0, timeline.cursor - extra);
                }

                return RecordResult.done(snapshot.size(), true);
            } finally {
                timeline.busy = false;
            }
        }
    }

    /**
     * Undoes up to {@code count} snapshots.
     */
    public static TraverseResult undo(EntityPlayer player, int count) {
        return traverse(player, Math.max(1, count), true);
    }

    /**
     * Redoes up to {@code count} snapshots.
     */
    public static TraverseResult redo(EntityPlayer player, int count) {
        return traverse(player, Math.max(1, count), false);
    }

    private static TraverseResult traverse(EntityPlayer player, int count, boolean undo) {
        Timeline timeline = timeline(player);
        synchronized (timeline) {
            if (timeline.busy) {
                return TraverseResult.busyResult();
            }

            timeline.busy = true;
            try {
                int steps = 0;
                int changed = 0;
                while (steps < count) {
                    if (undo && timeline.cursor <= 0) {
                        break;
                    }
                    if (!undo && timeline.cursor >= timeline.edits.size()) {
                        break;
                    }

                    int index = undo ? timeline.cursor - 1 : timeline.cursor;
                    Snapshot snapshot = timeline.edits.get(index);
                    changed += undo ? snapshot.undo() : snapshot.redo();
                    timeline.cursor = undo ? timeline.cursor - 1 : timeline.cursor + 1;
                    steps++;
                }

                return TraverseResult.done(steps, changed);
            } finally {
                timeline.busy = false;
            }
        }
    }

    private static Timeline timeline(EntityPlayer player) {
        return HISTORY.computeIfAbsent(player.getUniqueId(), ignored -> new Timeline());
    }

    /**
     * Clears history for a player.
     */
    public static void clear(EntityPlayer player) {
        HISTORY.remove(player.getUniqueId());
    }

    /**
     * Block iterator callback for snapshot entries.
     */
    @FunctionalInterface
    public interface AfterConsumer {
        void accept(int x, int y, int z, BlockState state);
    }

    private static final class Timeline {
        private final List<Snapshot> edits = new ArrayList<>();
        private int cursor;
        private boolean busy;
    }

    /**
     * Result of trying to record a new snapshot.
     */
    public record RecordResult(boolean busy, int changed, boolean recorded) {
        public static RecordResult busyResult() {
            return new RecordResult(true, 0, false);
        }

        public static RecordResult done(int changed, boolean recorded) {
            return new RecordResult(false, changed, recorded);
        }
    }

    /**
     * Result of undo or redo traversal.
     */
    public record TraverseResult(boolean busy, int steps, int changed) {
        public static TraverseResult busyResult() {
            return new TraverseResult(true, 0, 0);
        }

        public static TraverseResult done(int steps, int changed) {
            return new TraverseResult(false, steps, changed);
        }
    }
}

