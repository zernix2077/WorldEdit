package xyz.zernix.worldedit.message;

import xyz.zernix.worldedit.WorldEdit;

/**
 * Translation keys for block pattern parsing.
 */
public final class PatternMessages {
    public static final String ERR_NO_BLOCKS_SPECIFIED = WorldEdit.NAMESPACE + "pattern.error.no_blocks";
    public static final String ERR_WEIGHT_INVALID = WorldEdit.NAMESPACE + "pattern.error.weight_invalid";
    public static final String ERR_BLOCK_ID_INVALID = WorldEdit.NAMESPACE + "pattern.error.block_id_invalid";
    public static final String ERR_BLOCK_PROPERTIES_INVALID_STATE = WorldEdit.NAMESPACE + "pattern.error.block_props_invalid_state";
    public static final String ERR_BLOCK_PROPERTIES_INVALID_SYNTAX = WorldEdit.NAMESPACE + "pattern.error.block_props_invalid_syntax";
    public static final String ERR_BLOCK_PROPERTY_MISSING = WorldEdit.NAMESPACE + "pattern.error.block_prop_missing";
    public static final String ERR_BLOCK_PROPERTY_TYPE_MISMATCH = WorldEdit.NAMESPACE + "pattern.error.block_prop_type_mismatch";

    private PatternMessages() {
    }
}

