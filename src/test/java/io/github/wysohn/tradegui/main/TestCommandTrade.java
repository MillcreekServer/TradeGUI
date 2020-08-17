package io.github.wysohn.tradegui.main;

import fr.minuskube.inv.SmartInvsPlugin;
import io.github.wysohn.rapidframework2.bukkit.testutils.AbstractBukkitTest;
import io.github.wysohn.rapidframework2.bukkit.testutils.PluginMainTestBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({SmartInvsPlugin.class, Bukkit.class})
public class TestCommandTrade extends AbstractBukkitTest {

    private PluginMainTestBuilder builder;
    private final List<Listener> listenerList = new LinkedList<>();

    private ItemFactory mockItemFactory;

    @Before
    public void init() throws Exception {
        builder = PluginMainTestBuilder.create("trade",
                "trade", TradeGUIBridge.class)
                .before(context -> context.getCore().enable());

        doAnswer(invocation -> {
            if (invocation.getArguments()[1].getClass() != SmartInvsPlugin.class)
                return null;

            Listener listener = (Listener) invocation.getArguments()[0];
            listenerList.add(listener);
            return null;
        }).when(getMockPluginManager()).registerEvents(any(Listener.class), any());

        mockItemFactory = mock(ItemFactory.class);

        PowerMockito.mock(Bukkit.class);
        PowerMockito.when(Bukkit.class, "getItemFactory").thenReturn(mockItemFactory);
    }

    @Test
    public void startTrade() {
        String name2 = "player2";
        UUID uuid2 = UUID.randomUUID();
        Player player2 = player(uuid2, name2);

        builder.mockEvent(loginEvent(PLAYER_NAME, PLAYER_UUID))
                .mockEvent(joinEvent(player()))
                .mockEvent(loginEvent(name2, uuid2))
                .mockEvent(joinEvent(player2))
                .expect(runSubCommand(player(), "request player2"))
                .expect(runSubCommand(player2, "accept"))
                .test(wrap(player()), true);
    }
}
