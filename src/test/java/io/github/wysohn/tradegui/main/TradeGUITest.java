package io.github.wysohn.tradegui.main;

import io.github.wysohn.rapidframework3.bukkit.testutils.SimpleBukkitPluginMainTest;
import org.bukkit.Server;
import org.junit.Test;

public class TradeGUITest {
    @Test
    public void test() {
        new SimpleBukkitPluginMainTest<TradeGUI>() {
            @Override
            public TradeGUI instantiate(Server server) {
                return new TradeGUI(server);
            }
        }.enable();
    }
}