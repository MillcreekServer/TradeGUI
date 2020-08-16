package io.github.wysohn.tradegui.manager.gui;

import fr.minuskube.inv.ItemClickData;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.tradegui.api.GemsEconomyAPI;
import io.github.wysohn.tradegui.manager.trade.ITradeContent;
import io.github.wysohn.tradegui.manager.trade.ITrader;
import io.github.wysohn.tradegui.manager.trade.TradingItemStack;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GUIPair implements GUIPairNode.CancelHandle, GUIPairNode.TradeHandle {
    private final PluginMain main;
    private final ITrader trader1;
    private final ITrader trader2;
    private final Runnable onTradeEnd;

    private final GUIPairNode trader1GUI;
    private final GUIPairNode trader2GUI;

    private final ItemStack[] trader1RawContents = new ItemStack[GUIPairNode.CONTENTS_ROW * GUIPairNode.CONTENTS_COL];
    private final ItemStack[] trader2RawContents = new ItemStack[GUIPairNode.CONTENTS_ROW * GUIPairNode.CONTENTS_COL];

    private final Map<String, Double> trader1Currencies = new HashMap<>();
    private final Map<String, Double> trader2Currencies = new HashMap<>();

    private boolean trader1Ready = false;
    private boolean trader2Ready = false;

    public GUIPair(PluginMain main, ITrader trader1, ITrader trader2, Runnable onTradeEnd) {
        this.main = main;
        this.trader1 = trader1;
        this.trader2 = trader2;
        this.onTradeEnd = onTradeEnd;

        ItemStack head1 = trader1.getHeadItem();
        ItemStack head2 = trader2.getHeadItem();

        trader1GUI = new GUIPairNode(main,
                head1,
                head2,
                trader1RawContents,
                trader2RawContents,
                trader1Currencies,
                this,
                this);
        // REMEMBER that everything is reversed
        // head2 on left and head1 on right, etc.
        trader2GUI = new GUIPairNode(main,
                head2,
                head1,
                trader2RawContents,
                trader1RawContents,
                trader2Currencies,
                this,
                this);

        trader1GUI.setOtherGUI(trader2GUI);
        trader2GUI.setOtherGUI(trader1GUI);
    }

    public void begin() {
        trader1.openTradeGUI(trader1GUI.getGUI());
        trader2.openTradeGUI(trader2GUI.getGUI());
    }

    public void cancel() {
        trader1.closeTradeGUI(trader1GUI.getGUI());
        trader2.closeTradeGUI(trader2GUI.getGUI());
    }

    private Collection<ITradeContent> flat(Map<String, Double> currencies, ItemStack[] itemStacks) {
        Collection<ITradeContent> contents = new LinkedList<>();

        currencies.forEach((key, val) ->
                main.api().getAPI(GemsEconomyAPI.class)
                        .map(api -> api.toTradingMaterial(key, val))
                        .ifPresent(contents::add));

        Arrays.stream(itemStacks)
                .map(TradingItemStack::new)
                .forEach(contents::add);

        return contents;
    }

    @Override
    public void accept(GUIPairNode node, ItemClickData itemClickData) {
        if (node == trader1GUI) {
            if (node.isConfirmed())
                trader1Ready = !trader1Ready;
        } else if (node == trader2GUI) {
            if (node.isConfirmed())
                trader2Ready = !trader2Ready;
        } else {
            throw new RuntimeException("Unexpected node.");
        }

        if (trader1Ready && trader2Ready) {
            // swap items
            trader1.give(flat(trader2Currencies, trader2RawContents));
            trader2.give(flat(trader1Currencies, trader1RawContents));

            onTradeEnd.run();
        }
    }

    @Override
    public void accept(InventoryCloseEvent inventoryCloseEvent) {
        // trade failed. Give items back to original owners
        trader1.give(flat(trader1Currencies, trader1RawContents));
        trader2.give(flat(trader2Currencies, trader2RawContents));

        onTradeEnd.run();
    }
}
