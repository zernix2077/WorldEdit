package xyz.zernix.worldedit.message;

import xyz.zernix.worldedit.WorldEdit;

/**
 * Translation keys for undo/redo history actions.
 */
public final class HistoryMessages {
    public static final String ERR_BUSY = WorldEdit.NAMESPACE + "history.error.busy";
    public static final String UNDO_NOTHING = WorldEdit.NAMESPACE + "history.undo.nothing";
    public static final String REDO_NOTHING = WorldEdit.NAMESPACE + "history.redo.nothing";
    public static final String UNDO_SUCCESS = WorldEdit.NAMESPACE + "history.undo.success";
    public static final String REDO_SUCCESS = WorldEdit.NAMESPACE + "history.redo.success";

    private HistoryMessages() {
    }
}

