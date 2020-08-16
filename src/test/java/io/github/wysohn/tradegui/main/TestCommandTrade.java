package io.github.wysohn.tradegui.main;

import fr.minuskube.inv.SmartInvsPlugin;
import io.github.wysohn.rapidframework2.bukkit.testutils.AbstractBukkitTest;
import io.github.wysohn.rapidframework2.bukkit.testutils.PluginMainTestBuilder;
import io.github.wysohn.tradegui.manager.gui.GUIPair;
import io.github.wysohn.tradegui.manager.trade.TradingManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static io.github.wysohn.rapidframework2.bukkit.testutils.PluginMainTestBuilder.mimicEvent;
import static org.mockito.Mockito.*;

public class TestCommandTrade extends AbstractBukkitTest {

    private PluginMainTestBuilder builder;
    private final List<Listener> listenerList = new LinkedList<>();

    @Before
    public void init() {
        builder = PluginMainTestBuilder.create("trade",
                "trade", TradeGUIBridge.class)
                .before(context -> context.getCore().enable())
                .after(context -> context.getCore().disable());

        doAnswer(invocation -> {
            if (invocation.getArguments()[1].getClass() != SmartInvsPlugin.class)
                return null;

            Listener listener = (Listener) invocation.getArguments()[0];
            listenerList.add(listener);
            return null;
        }).when(getMockPluginManager()).registerEvents(any(Listener.class), any());
    }

    @Test
    public void startTrade() {
        Inventory mockInv = mock(Inventory.class);

        String name2 = "player2";
        UUID uuid2 = UUID.randomUUID();
        Player player2 = player(uuid2, name2);
        Inventory mockInv2 = mock(Inventory.class);

        ItemStack mockItemInHand = mock(ItemStack.class);
        ItemStack mockItemClicked = mock(ItemStack.class);

        builder.mockEvent(loginEvent(PLAYER_NAME, PLAYER_UUID))
                .mockEvent(joinEvent(player()))
                .mockEvent(loginEvent(name2, uuid2))
                .mockEvent(joinEvent(player2))
                .expect(runSubCommand(player(), "request player2"))
                .expect(runSubCommand(player2, "accept"))
                .expect((context) -> {
                    listenerList.forEach(listener -> mimicEvent(listener,
                            clickEvent(player(), mockInv, 3, 4, mockItemInHand, null)));
                    return true;
                })
                .expect(context -> context.getMain().getManager(TradingManager.class)
                        .map(tradingManager -> tradingManager.getCurrentTradeGUI(uuid2))
                        .map(GUIPair::getTrader1RawContents)
                        .map(stacks -> stacks[2] == mockItemInHand)
                        .orElse(false))
                .test(wrap(player()), true);
    }

    private InventoryClickEvent clickEvent(Player player, Inventory mockInv,
                                           int row, int col,
                                           ItemStack inHand, ItemStack clicked) {
        InventoryAction action = null;
        if (inHand != null && clicked != null) {
            action = InventoryAction.SWAP_WITH_CURSOR;
        } else if (inHand != null) {
            action = InventoryAction.DROP_ALL_CURSOR;
        } else if (clicked != null) {
            action = InventoryAction.PICKUP_ALL;
        } else {
            action = InventoryAction.UNKNOWN;
        }

        InventoryView mockView = new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return mockInv;
            }

            @Override
            public Inventory getBottomInventory() {
                return null;
            }

            @Override
            public HumanEntity getPlayer() {
                return player;
            }

            @Override
            public InventoryType getType() {
                return InventoryType.CHEST;
            }

            @Override
            public String getTitle() {
                return "Mocked View";
            }
        };

        return new InventoryClickEvent(mockView, InventoryType.SlotType.CONTAINER,
                9 * row + col, ClickType.LEFT, action);
    }
}
