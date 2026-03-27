package xyz.zernix.worldedit.command.selection;

import xyz.zernix.worldedit.Selection;

/**
 * Sets the second selection point.
 */
public final class Pos2Command extends SetPosCommand {
    public Pos2Command() {
        super("pos2", "worldedit:command.pos2.description", Selection.PosKind.SECOND);
    }
}

