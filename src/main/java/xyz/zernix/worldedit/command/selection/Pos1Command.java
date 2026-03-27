package xyz.zernix.worldedit.command.selection;

import xyz.zernix.worldedit.Selection;

/**
 * Sets the first selection point.
 */
public final class Pos1Command extends SetPosCommand {
    public Pos1Command() {
        super("pos1", "worldedit:command.pos1.description", Selection.PosKind.FIRST);
    }
}

