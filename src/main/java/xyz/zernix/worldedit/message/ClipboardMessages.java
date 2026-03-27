package xyz.zernix.worldedit.message;

import xyz.zernix.worldedit.WorldEdit;

/**
 * Translation keys for clipboard commands.
 */
public final class ClipboardMessages {
    public static final String CLEARED = WorldEdit.NAMESPACE + "clipboard.cleared";
    public static final String ALREADY_EMPTY = WorldEdit.NAMESPACE + "clipboard.already_empty";
    public static final String ROTATED = WorldEdit.NAMESPACE + "clipboard.rotated";
    public static final String FLIPPED = WorldEdit.NAMESPACE + "clipboard.flipped";
    public static final String ERR_ROTATE_INVALID = WorldEdit.NAMESPACE + "clipboard.error.rotate_invalid";

    private ClipboardMessages() {
    }
}

