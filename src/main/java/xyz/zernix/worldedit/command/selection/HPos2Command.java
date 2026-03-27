package xyz.zernix.worldedit.command.selection;

import xyz.zernix.worldedit.Selection;

/**
 * Sets the second selection point to the first solid block in sight.
 */
public final class HPos2Command extends SetHPosCommand {
    public HPos2Command() {
        super("hpos2", "worldedit:command.hpos2.description", Selection.PosKind.SECOND);
    }
}

