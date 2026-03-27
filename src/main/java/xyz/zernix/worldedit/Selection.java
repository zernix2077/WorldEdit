package xyz.zernix.worldedit;

import org.allaymc.api.debugshape.DebugBox;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.player.Player;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import xyz.zernix.worldedit.message.SelectionMessages;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player WorldEdit selection state.
 */
public final class Selection {
    private static final Map<UUID, State> STATE = new ConcurrentHashMap<>();

    private Selection() {
    }

    /**
     * Selection position kind.
     */
    public enum PosKind {
        FIRST,
        SECOND
    }

    /**
     * Inclusive integer cuboid for block selections.
     */
    public record Bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        public boolean isValid() {
            return minX <= maxX && minY <= maxY && minZ <= maxZ;
        }

        public int volume() {
            return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        }

        public Bounds shifted(int dx, int dy, int dz) {
            return new Bounds(minX + dx, maxX + dx, minY + dy, maxY + dy, minZ + dz, maxZ + dz);
        }
    }

    /**
     * Returns current selection state for a player.
     */
    public static State of(EntityPlayer player) {
        return STATE.computeIfAbsent(player.getUniqueId(), ignored -> new State());
    }

    /**
     * Updates one selection corner and sends notifications.
     */
    public static void setPos(EntityPlayer player, PosKind kind, Vector3ic pos) {
        setPos(player, kind, pos, false);
    }

    /**
     * Updates one selection corner and optionally suppresses repeated notifications.
     */
    public static void setPos(EntityPlayer player, PosKind kind, Vector3ic pos, boolean silent) {
        if (!player.getLocation().dimension().isInWorld(pos.x(), pos.y(), pos.z())) {
            player.sendTranslatable(SelectionMessages.ERR_POS_OUTSIDE_WORLD, pos.x(), pos.y(), pos.z());
            return;
        }

        State state = of(player);
        Vector3i copied = new Vector3i(pos);
        boolean updated;
        if (kind == PosKind.FIRST) {
            updated = state.pos1 == null || !state.pos1.equals(copied);
            if (updated) {
                state.pos1 = copied;
            }
        } else {
            updated = state.pos2 == null || !state.pos2.equals(copied);
            if (updated) {
                state.pos2 = copied;
            }
        }

        if (!silent || updated) {
            if (kind == PosKind.FIRST) {
                player.sendTranslatable(SelectionMessages.SET_FIRST, copied.x(), copied.y(), copied.z());
            } else {
                player.sendTranslatable(SelectionMessages.SET_SECOND, copied.x(), copied.y(), copied.z());
            }

            if (state.pos1 != null && state.pos2 != null) {
                int dx = Math.abs(state.pos1.x - state.pos2.x) + 1;
                int dy = Math.abs(state.pos1.y - state.pos2.y) + 1;
                int dz = Math.abs(state.pos1.z - state.pos2.z) + 1;
                player.sendTranslatable(SelectionMessages.SET_TOTAL, dx * dy * dz);
            }
        }

        refreshVisual(player, state);
    }

    /**
     * Updates first position.
     */
    public static void setFirst(EntityPlayer player, Vector3ic pos, boolean silent) {
        setPos(player, PosKind.FIRST, pos, silent);
    }

    /**
     * Updates second position.
     */
    public static void setSecond(EntityPlayer player, Vector3ic pos, boolean silent) {
        setPos(player, PosKind.SECOND, pos, silent);
    }

    /**
     * Sets both selection corners.
     */
    public static void setBoth(EntityPlayer player, Vector3ic pos1, Vector3ic pos2) {
        State state = of(player);
        state.pos1 = new Vector3i(pos1);
        state.pos2 = new Vector3i(pos2);
        refreshVisual(player, state);

        int dx = Math.abs(pos1.x() - pos2.x()) + 1;
        int dy = Math.abs(pos1.y() - pos2.y()) + 1;
        int dz = Math.abs(pos1.z() - pos2.z()) + 1;
        player.sendTranslatable(SelectionMessages.SET_TOTAL, dx * dy * dz);
    }

    /**
     * Returns selected bounds or {@code null} when positions are not fully selected.
     */
    public static Bounds bounds(EntityPlayer player) {
        State state = of(player);
        if (state.pos1 == null || state.pos2 == null) {
            player.sendTranslatable(SelectionMessages.ERR_SET_REQUIRED_BOTH);
            return null;
        }

        return new Bounds(
                Math.min(state.pos1.x, state.pos2.x),
                Math.max(state.pos1.x, state.pos2.x),
                Math.min(state.pos1.y, state.pos2.y),
                Math.max(state.pos1.y, state.pos2.y),
                Math.min(state.pos1.z, state.pos2.z),
                Math.max(state.pos1.z, state.pos2.z)
        );
    }

    /**
     * Applies bounds back to player selection if in-world.
     */
    public static boolean applyBounds(EntityPlayer player, Bounds bounds) {
        var dimension = player.getLocation().dimension();
        if (!dimension.isInWorld(bounds.minX, bounds.minY, bounds.minZ)) {
            player.sendTranslatable(SelectionMessages.ERR_POS_OUTSIDE_WORLD, bounds.minX, bounds.minY, bounds.minZ);
            return false;
        }
        if (!dimension.isInWorld(bounds.maxX, bounds.maxY, bounds.maxZ)) {
            player.sendTranslatable(SelectionMessages.ERR_POS_OUTSIDE_WORLD, bounds.maxX, bounds.maxY, bounds.maxZ);
            return false;
        }

        setBoth(player, new Vector3i(bounds.minX, bounds.minY, bounds.minZ), new Vector3i(bounds.maxX, bounds.maxY, bounds.maxZ));
        return true;
    }

    /**
     * Clears all in-memory selection state and debug visuals.
     */
    public static void clearAll() {
        STATE.clear();
    }

    private static void clearVisual(EntityPlayer player, State state) {
        if (state.visual == null) {
            return;
        }

        Player viewer = player.getController();
        if (viewer != null) {
            viewer.removeDebugShape(state.visual);
            state.visual.removeViewer(viewer, false);
        }
        state.visual = null;
    }

    private static void refreshVisual(EntityPlayer player, State state) {
        if (!state.visible) {
            clearVisual(player, state);
            return;
        }

        BoxData boxData = calcBoxData(state);
        if (boxData == null) {
            clearVisual(player, state);
            return;
        }

        Player viewer = player.getController();
        if (viewer == null) {
            return;
        }

        DebugBox box = state.visual;
        if (box == null) {
            box = new DebugBox(boxData.center(), Color.RED, 1.0f, boxData.bounds());
            state.visual = box;
        } else {
            box.setColor(Color.RED);
            box.setPosition(boxData.center());
            box.setScale(1.0f);
            box.setBoxBounds(boxData.bounds());
        }
        viewer.viewDebugShape(box);
    }

    private static BoxData calcBoxData(State state) {
        if (state.pos1 == null && state.pos2 == null) {
            return null;
        }

        if (state.pos1 != null && state.pos2 == null) {
            return new BoxData(
                    new Vector3f(state.pos1.x, state.pos1.y, state.pos1.z),
                    new Vector3f(1f, 1f, 1f)
            );
        }
        if (state.pos1 == null) {
            return new BoxData(
                    new Vector3f(state.pos2.x, state.pos2.y, state.pos2.z),
                    new Vector3f(1f, 1f, 1f)
            );
        }

        int minX = Math.min(state.pos1.x, state.pos2.x);
        int maxX = Math.max(state.pos1.x, state.pos2.x);
        int minY = Math.min(state.pos1.y, state.pos2.y);
        int maxY = Math.max(state.pos1.y, state.pos2.y);
        int minZ = Math.min(state.pos1.z, state.pos2.z);
        int maxZ = Math.max(state.pos1.z, state.pos2.z);

        return new BoxData(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX - minX + 1f, maxY - minY + 1f, maxZ - minZ + 1f)
        );
    }

    private record BoxData(Vector3f center, Vector3f bounds) {
    }

    /**
     * Mutable selection state for one player.
     */
    public static final class State {
        private Vector3i pos1;
        private Vector3i pos2;
        private DebugBox visual;
        private boolean visible = true;

        /**
         * Returns first selected position or {@code null}.
         */
        public Vector3ic pos1() {
            return pos1;
        }

        /**
         * Returns second selected position or {@code null}.
         */
        public Vector3ic pos2() {
            return pos2;
        }

        /**
         * Removes currently visible selection box.
         */
        public void clearVisual(EntityPlayer player) {
            Selection.clearVisual(player, this);
        }

        /**
         * Toggles selection box visibility and returns the new state.
         */
        public boolean toggleVisual(EntityPlayer player) {
            this.visible = !this.visible;
            if (visible) {
                Selection.refreshVisual(player, this);
            } else {
                Selection.clearVisual(player, this);
            }
            return visible;
        }
    }
}

