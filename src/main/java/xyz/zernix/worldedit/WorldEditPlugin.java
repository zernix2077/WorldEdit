package xyz.zernix.worldedit;

import org.allaymc.api.command.Command;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import xyz.zernix.worldedit.command.clipboard.*;
import xyz.zernix.worldedit.command.general.*;
import xyz.zernix.worldedit.command.navigation.*;
import xyz.zernix.worldedit.command.region.*;
import xyz.zernix.worldedit.command.selection.*;
import xyz.zernix.worldedit.listener.WandListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin entry point for Java WorldEdit.
 */
public final class WorldEditPlugin extends Plugin {
    private final List<Command> commands = List.of(
            new UndoCommand(),
            new RedoCommand(),

            new WandCommand(),
            new Pos1Command(),
            new Pos2Command(),
            new HPos1Command(),
            new HPos2Command(),
            new ClearSelBoxCommand(),
            new SelBoxToggleCommand(),
            new ExpandCommand(),
            new ContractCommand(),
            new ShiftCommand(),
            new OutsetCommand(),
            new InsetCommand(),
            new TrimCommand(),
            new CountCommand(),

            new UnstuckCommand(),
            new AscendCommand(),
            new DescendCommand(),
            new CeilCommand(),
            new ThruCommand(),
            new JumpToCommand(),
            new UpCommand(),

            new ClearClipboardCommand(),
            new RotateCommand(),
            new FlipCommand(),

            new SetCommand(),
            new ReplaceCommand(),
            new SphereCommand(),
            new HSphereCommand(),
            new CylCommand(),
            new HCylCommand(),
            new ConeCommand(),
            new PyramidCommand(),
            new HPyramidCommand(),
            new CopyCommand(),
            new CutCommand(),
            new PasteCommand(),
            new LineCommand(),
            new OverlayCommand(),
            new CenterCommand(),
            new WallsCommand(),
            new FacesCommand(),
            new SmoothCommand(),
            new MoveCommand(),
            new StackCommand(),
            new HollowCommand(),
            new RegenCommand()
    );

    private final WandListener wandListener = new WandListener();

    @Override
    public void onEnable() {
        for (Command command : commands) {
            Registries.COMMANDS.register(command);
        }
        Server.getInstance().getEventBus().registerListener(wandListener);
        getPluginLogger().info("Registered {} WorldEdit commands", commands.size());
    }

    @Override
    public void onDisable() {
        Server.getInstance().getEventBus().unregisterListener(wandListener);
        for (Command command : commands) {
            Registries.COMMANDS.unregister(command.getName());
        }
        Selection.clearAll();
        getPluginLogger().info("WorldEdit disabled");
    }
}

