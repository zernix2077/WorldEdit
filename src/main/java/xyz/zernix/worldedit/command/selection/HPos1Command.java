package xyz.zernix.worldedit.command.selection;

import xyz.zernix.worldedit.Selection;

/**
 * Sets the first selection point to the first solid block in sight.
 */
public final class HPos1Command extends SetHPosCommand {
    public HPos1Command() {
        super("hpos1", "worldedit:command.hpos1.description", Selection.PosKind.FIRST);
    }
}

