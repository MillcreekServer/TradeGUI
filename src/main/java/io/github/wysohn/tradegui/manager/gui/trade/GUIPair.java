package io.github.wysohn.tradegui.manager.gui.trade;

import fr.minuskube.inv.ItemClickData;
import io.github.wysohn.rapidframework3.bukkit.utils.InventoryUtil;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import io.github.wysohn.rapidframework3.interfaces.ICommandSender;
import io.github.wysohn.tradegui.main.TradeGUILangs;
import io.github.wysohn.tradegui.manager.EconomyMediator;
import io.github.wysohn.tradegui.manager.trade.ITradeContent;
import io.github.wysohn.tradegui.manager.trade.ITrader;
import io.github.wysohn.tradegui.manager.trade.TradingItemStack;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GUIPair implements GUIPairNode.CloseHandle, GUIPairNode.TradeHandle {
    private final PluginMain main;
    private final ITrader trader1;
    private final ITrader trader2;
    private final Consumer<GUIPair> onTradeEnd;

    private final GUIPairNode trader1GUI;
    private final GUIPairNode trader2GUI;

    private final ItemStack[] trader1RawContents = new ItemStack[GUIPairNode.CONTENTS_ROW * GUIPairNode.CONTENTS_COL];
    private final ItemStack[] trader2RawContents = new ItemStack[GUIPairNode.CONTENTS_ROW * GUIPairNode.CONTENTS_COL];

    private final Map<String, Double> trader1Currencies = new HashMap<>();
    private final Map<String, Double> trader2Currencies = new HashMap<>();

    private final ItemStack trader1TradeButton;
    private final ItemStack trader2TradeButton;

    private boolean trader1Ready = false;
    private boolean trader2Ready = false;

    private boolean end = false;

    public GUIPair(PluginMain main, ITrader trader1, ITrader trader2, Consumer<GUIPair> onTradeEnd) {
        this.main = main;
        this.trader1 = trader1;
        this.trader2 = trader2;
        this.onTradeEnd = onTradeEnd;

        ItemStack head1 = trader1.getHeadItem();
        ItemStack head2 = trader2.getHeadItem();

        trader1TradeButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        updateTradeButtomItem(trader1, trader1TradeButton, false, false);
        trader2TradeButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        updateTradeButtomItem(trader2, trader2TradeButton, false, false);

        initCurrencies(trader1Currencies);
        initCurrencies(trader2Currencies);

        trader1GUI = new GUIPairNode(main,
                head1,
                head2,
                trader1TradeButton,
                trader1RawContents,
                trader2RawContents,
                trader1Currencies,
                trader2Currencies,
                normalizeForTrader(trader1),
                this,
                this);
        // REMEMBER that everything is reversed
        // head2 on left and head1 on right, etc.
        trader2GUI = new GUIPairNode(main,
                head2,
                head1,
                trader2TradeButton,
                trader2RawContents,
                trader1RawContents,
                trader2Currencies,
                trader1Currencies,
                normalizeForTrader(trader2),
                this,
                this);

        trader1GUI.setOtherGUI(trader2GUI);
        trader2GUI.setOtherGUI(trader1GUI);
    }

    private void initCurrencies(Map<String, Double> traderCurrencies) {
        main.getMediator(EconomyMediator.class)
                .map(EconomyMediator::getCurrencies)
                .ifPresent(currencies -> currencies.forEach(currency -> traderCurrencies.put(currency, 0.0)));
    }

    private BiFunction<String, Double, Double> normalizeForTrader(ITrader trader) {
        // take the smaller value between current balance and amount selected
        return (curr, amount) -> Math.max(0.0, Math.min(main.getMediator(EconomyMediator.class)
                        .map(mediator -> mediator.balance(trader.getUuid(), curr))
                        .orElse(0.0),
                amount));
    }

    public void begin() {
        trader1.openTradeGUI(trader1GUI.getGUI());
        trader2.openTradeGUI(trader2GUI.getGUI());
    }

    private Collection<ITradeContent> flat(ITrader currencyOwner,
                                           Map<String, Double> currencies,
                                           ItemStack[] itemStacks) {
        Collection<ITradeContent> contents = new LinkedList<>();

        currencies.forEach((key, val) ->
                main.getMediator(EconomyMediator.class)
                        .map(mediator -> mediator.toTradingContent(currencyOwner.getUuid(), key, val))
                        .ifPresent(contents::add));

        Arrays.stream(itemStacks)
                .map(TradingItemStack::new)
                .forEach(contents::add);

        return contents;
    }

    private Collection<ITradeContent> flat(ItemStack[] itemStacks) {
        Collection<ITradeContent> contents = new LinkedList<>();

        Arrays.stream(itemStacks)
                .map(TradingItemStack::new)
                .forEach(contents::add);

        return contents;
    }

    private void updateTradeButtomItem(
            ICommandSender sender, ItemStack item,
            boolean confirmed, boolean ready) {
        if (confirmed && ready) {
            item.setType(Material.GREEN_STAINED_GLASS_PANE);
            InventoryUtil.parseFirstToItemTitle(main.lang(),
                    sender,
                    TradeGUILangs.GUI_Trade_Ready_Title,
                    item);
            InventoryUtil.parseToItemLores(main.lang(),
                    sender,
                    TradeGUILangs.GUI_Trade_Ready_Lore,
                    item,
                    true);
        } else if (confirmed) {
            item.setType(Material.YELLOW_STAINED_GLASS_PANE);
            InventoryUtil.parseFirstToItemTitle(main.lang(),
                    sender,
                    TradeGUILangs.GUI_Trade_NotReady_Title,
                    item);
            InventoryUtil.parseToItemLores(main.lang(),
                    sender,
                    TradeGUILangs.GUI_Trade_NotReady_Lore2,
                    item,
                    true);
        } else {
            item.setType(Material.YELLOW_STAINED_GLASS_PANE);
            InventoryUtil.parseFirstToItemTitle(main.lang(),
                    sender,
                    TradeGUILangs.GUI_Trade_NotReady_Title,
                    item);
            InventoryUtil.parseToItemLores(main.lang(),
                    sender,
                    TradeGUILangs.GUI_Trade_NotReady_Lore1,
                    item,
                    true);
        }
    }

    @Override
    public void onTrade(GUIPairNode node, ItemClickData itemClickData) {
        if (node == trader1GUI) {
            trader1Ready = !trader1Ready && node.isConfirmed();

            updateTradeButtomItem(trader1, trader1TradeButton, node.isConfirmed(), trader1Ready);
        } else if (node == trader2GUI) {
            trader2Ready = !trader2Ready && node.isConfirmed();

            updateTradeButtomItem(trader2, trader2TradeButton, node.isConfirmed(), trader2Ready);
        } else {
            throw new RuntimeException("Unexpected node.");
        }

        if (trader1Ready && trader2Ready) {
            boolean tradable = verifyCurrency(trader1, trader1Currencies) && verifyCurrency(trader2, trader2Currencies);
            try {
                if (tradable) {
                    // swap items and currencies
                    trader1.give(flat(trader2, trader2Currencies, trader2RawContents));
                    trader2.give(flat(trader1, trader1Currencies, trader1RawContents));
                } else {
                    main.lang().sendMessage(trader1, TradeGUILangs.Trade_Result_CurrencyMismatch);
                    main.lang().sendMessage(trader2, TradeGUILangs.Trade_Result_CurrencyMismatch);
                }
            } finally {
                // WARNING) must mark it end or onClose() will be called and will duplicate the items
                end = true;

                trader1.closeTradeGUI(trader1GUI.getGUI());
                trader2.closeTradeGUI(trader2GUI.getGUI());

                if (!tradable) {
                    //Something went wrong. Restore items
                    trader1.give(flat(trader1RawContents));
                    trader2.give(flat(trader2RawContents));

                    // caller of callback can see that the trade failed
                    trader1Ready = trader2Ready = false;
                }

                onTradeEnd.accept(this);
            }
        }
    }

    private boolean verifyCurrency(ITrader trader, Map<String, Double> traderCurrencies) {
        EconomyMediator mediator = main.getMediator(EconomyMediator.class)
                .orElseThrow(RuntimeException::new);

        for (Map.Entry<String, Double> entry : traderCurrencies.entrySet()) {
            String currencyName = entry.getKey();
            double amount = Math.max(0.0, entry.getValue());

            if (Math.max(0.0, mediator.balance(trader.getUuid(), currencyName)) < amount)
                return false;
        }

        return true;
    }

    @Override
    public void onClose(GUIPairNode node, InventoryCloseEvent event) {
        stopNow();
    }

    /**
     * Cancel current trade right away.
     */
    public void stopNow() {
        // WARNING) must be called once to avoid duplication (closeTradeGUI() will fire onClose() again)
        if (end)
            return; // so block it with boolean flag
        end = true;

        // currency doesn't have to be paid back since transaction never occurred
        trader1.give(flat(trader1RawContents));
        trader2.give(flat(trader2RawContents));

        trader1.closeTradeGUI(trader1GUI.getGUI());
        trader2.closeTradeGUI(trader2GUI.getGUI());

        onTradeEnd.accept(this);
    }

    public ITrader getTrader1() {
        return trader1;
    }

    public ITrader getTrader2() {
        return trader2;
    }

    public GUIPairNode getTrader1GUI() {
        return trader1GUI;
    }

    public GUIPairNode getTrader2GUI() {
        return trader2GUI;
    }

    public ItemStack[] getTrader1RawContents() {
        return trader1RawContents;
    }

    public ItemStack[] getTrader2RawContents() {
        return trader2RawContents;
    }

    public Map<String, Double> getTrader1Currencies() {
        return trader1Currencies;
    }

    public Map<String, Double> getTrader2Currencies() {
        return trader2Currencies;
    }

    public boolean isTrader1Ready() {
        return trader1Ready;
    }

    public boolean isTrader2Ready() {
        return trader2Ready;
    }
}
