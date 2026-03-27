package xyz.zernix.worldedit.command.arg;

/**
 * Marker for compact command flags such as {@code -h}.
 */
public interface Flag {
    /**
     * Single-character flag token.
     */
    char token();
}

