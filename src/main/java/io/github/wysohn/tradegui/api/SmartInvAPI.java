package io.github.wysohn.tradegui.api;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryProvider;
import io.github.wysohn.rapidframework3.core.api.ExternalAPI;
import io.github.wysohn.rapidframework3.core.main.PluginMain;

public class SmartInvAPI extends ExternalAPI {
    public SmartInvAPI(PluginMain main, String pluginName) {
        super(main, pluginName);
    }

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    public static abstract class GUI implements InventoryProvider {
        public abstract SmartInventory getGUI();
    }
}
