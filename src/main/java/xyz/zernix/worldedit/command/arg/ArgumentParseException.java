package xyz.zernix.worldedit.command.arg;

/**
 * Exception raised when a command argument cannot be parsed.
 */
public final class ArgumentParseException extends Exception {
    private final String key;
    private final Object[] args;

    public ArgumentParseException(String key, Object... args) {
        super(key);
        this.key = key;
        this.args = args;
    }

    public String key() {
        return key;
    }

    public Object[] args() {
        return args;
    }
}

