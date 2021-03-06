package io.github.wysohn.%pluginname%.main;

import io.github.wysohn.rapidframework2.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework2.bukkit.main.BukkitPluginBridge;
import io.github.wysohn.rapidframework2.core.interfaces.plugin.IPluginManager;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.command.SubCommand;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class $pluginname$Bridge extends BukkitPluginBridge {
    public $pluginname$Bridge(AbstractBukkitPlugin bukkit) {
        super(bukkit);
    }

    public $pluginname$Bridge(String pluginName,
                              String pluginDescription,
                              String mainCommand,
                              String adminPermission,
                              Logger logger,
                              File dataFolder,
                              IPluginManager iPluginManager,
                              AbstractBukkitPlugin bukkit) {
        super(pluginName, pluginDescription, mainCommand, adminPermission, logger, dataFolder, iPluginManager, bukkit);
    }

    @Override
    protected PluginMain init(PluginMain.Builder builder) {
        return builder
                //TODO do something here
                .build();
    }

    @Override
    protected void registerCommands(List<SubCommand> list) {
        //TODO add commands
    }
}
