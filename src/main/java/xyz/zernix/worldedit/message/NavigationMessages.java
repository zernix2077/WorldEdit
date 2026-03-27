package xyz.zernix.worldedit.message;

import xyz.zernix.worldedit.WorldEdit;

/**
 * Translation keys for navigation commands.
 */
public final class NavigationMessages {
    public static final String UNSTUCK_MOVED = WorldEdit.NAMESPACE + "navigation.unstuck.moved";
    public static final String UNSTUCK_FAILED = WorldEdit.NAMESPACE + "navigation.unstuck.failed";

    public static final String ASCEND_OBSTRUCTED = WorldEdit.NAMESPACE + "navigation.ascend.obstructed";
    public static final String ASCEND_MOVED = WorldEdit.NAMESPACE + "navigation.ascend.moved";
    public static final String DESCEND_OBSTRUCTED = WorldEdit.NAMESPACE + "navigation.descend.obstructed";
    public static final String DESCEND_MOVED = WorldEdit.NAMESPACE + "navigation.descend.moved";

    public static final String CEIL_MOVED = WorldEdit.NAMESPACE + "navigation.ceil.moved";
    public static final String CEIL_OBSTRUCTED = WorldEdit.NAMESPACE + "navigation.ceil.obstructed";
    public static final String THRU_MOVED = WorldEdit.NAMESPACE + "navigation.thru.moved";
    public static final String THRU_OBSTRUCTED = WorldEdit.NAMESPACE + "navigation.thru.obstructed";

    public static final String JUMP_TO_MOVED = WorldEdit.NAMESPACE + "navigation.jumpto.moved";
    public static final String JUMP_TO_NONE = WorldEdit.NAMESPACE + "navigation.jumpto.none";
    public static final String JUMP_TO_OBSTRUCTED = WorldEdit.NAMESPACE + "navigation.jumpto.obstructed";

    public static final String UP_MOVED = WorldEdit.NAMESPACE + "navigation.up.moved";
    public static final String UP_OBSTRUCTED = WorldEdit.NAMESPACE + "navigation.up.obstructed";

    private NavigationMessages() {
    }
}

