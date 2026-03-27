package xyz.zernix.worldedit.command;

import org.allaymc.api.command.Command;
import xyz.zernix.worldedit.WorldEdit;

import java.util.List;

/**
 * Shared root for all WorldEdit commands.
 */
public abstract class WorldEditCommand extends Command {
    protected WorldEditCommand(String name, String descriptionKey) {
        this(name, descriptionKey, List.of());
    }

    protected WorldEditCommand(String name, String descriptionKey, List<String> aliases) {
        super(name, descriptionKey, WorldEdit.PERMISSION);
        this.aliases.addAll(aliases);
    }
}

