package xyz.zernix.worldedit.message;

import xyz.zernix.worldedit.WorldEdit;

/**
 * Translation keys for selection commands and wand actions.
 */
public final class SelectionMessages {
    public static final String SET_FIRST = WorldEdit.NAMESPACE + "selection.set.first";
    public static final String SET_SECOND = WorldEdit.NAMESPACE + "selection.set.second";
    public static final String SET_TOTAL = WorldEdit.NAMESPACE + "selection.set.total";

    public static final String ERR_SET_REQUIRED_BOTH = WorldEdit.NAMESPACE + "selection.error.required_both";
    public static final String ERR_SET_REQUIRED_SINGLE = WorldEdit.NAMESPACE + "selection.error.required_single";
    public static final String ERR_POS_OUTSIDE_WORLD = WorldEdit.NAMESPACE + "selection.error.pos_outside_world";

    public static final String WAND_NAME = WorldEdit.NAMESPACE + "selection.wand.name";
    public static final String WAND_GIVE_SUCCESS = WorldEdit.NAMESPACE + "selection.wand.give_success";
    public static final String WAND_GIVE_FAILURE = WorldEdit.NAMESPACE + "selection.wand.give_failure";

    public static final String CLEARED = WorldEdit.NAMESPACE + "selection.cleared";
    public static final String TOGGLE_ON = WorldEdit.NAMESPACE + "selection.toggle.on";
    public static final String TOGGLE_OFF = WorldEdit.NAMESPACE + "selection.toggle.off";
    public static final String HPOS_NO_BLOCK = WorldEdit.NAMESPACE + "selection.hpos.no_block";
    public static final String EXPAND_OBSTRUCTED = WorldEdit.NAMESPACE + "selection.expand.obstructed";
    public static final String EXPANDED = WorldEdit.NAMESPACE + "selection.expanded";
    public static final String CONTRACTED = WorldEdit.NAMESPACE + "selection.contracted";
    public static final String SHIFTED = WorldEdit.NAMESPACE + "selection.shifted";
    public static final String OUTSET = WorldEdit.NAMESPACE + "selection.outset";
    public static final String INSET = WorldEdit.NAMESPACE + "selection.inset";
    public static final String TRIM_NO_BLOCKS = WorldEdit.NAMESPACE + "selection.trim.no_blocks";
    public static final String TRIM_DONE = WorldEdit.NAMESPACE + "selection.trim.done";
    public static final String COUNTED = WorldEdit.NAMESPACE + "selection.counted";

    public static final String ERR_DIRECTION_INVALID = WorldEdit.NAMESPACE + "selection.error.direction_invalid";
    public static final String ERR_MASK_NO_BLOCKS_SPECIFIED = WorldEdit.NAMESPACE + "selection.error.mask_no_blocks";
    public static final String ERR_MASK_BLOCK_ID_INVALID = WorldEdit.NAMESPACE + "selection.error.mask_block_id_invalid";
    public static final String ERR_MASK_BLOCK_PROPS_INVALID_STATE = WorldEdit.NAMESPACE + "selection.error.mask_block_props_invalid_state";
    public static final String ERR_MASK_BLOCK_PROPS_INVALID_SYNTAX = WorldEdit.NAMESPACE + "selection.error.mask_block_props_invalid_syntax";
    public static final String ERR_MASK_BLOCK_PROP_MISSING = WorldEdit.NAMESPACE + "selection.error.mask_block_prop_missing";
    public static final String ERR_MASK_BLOCK_PROP_TYPE_MISMATCH = WorldEdit.NAMESPACE + "selection.error.mask_block_prop_type_mismatch";

    private SelectionMessages() {
    }
}

