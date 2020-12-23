package io.github.wysohn.tradegui.manager.gui.trade;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.ItemClickData;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.SlotPos;
import io.github.wysohn.rapidframework3.bukkit.data.BukkitWrapper;
import io.github.wysohn.rapidframework3.bukkit.utils.InventoryUtil;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import io.github.wysohn.rapidframework3.interfaces.language.ILang;
import io.github.wysohn.tradegui.api.economy.RealEconomyAPI;
import io.github.wysohn.tradegui.main.TradeGUILangs;
import io.github.wysohn.tradegui.manager.gui.AbstractGUI;
import io.github.wysohn.tradegui.manager.gui.amount.GUIAmountSelector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GUIPairNode extends AbstractGUI implements Consumer<InventoryClickEvent> {
    public static final int CONTENTS_ROW = 2;
    public static final int CONTENTS_COL = 3;

    public static final SlotPos LEFT_FROM = SlotPos.of(3, 1);
    public static final SlotPos RIGHT_FROM = SlotPos.of(3, 5);

    private final PluginMain main;
    private GUIPairNode otherGUI;

    private final ItemStack traderHeadItem;
    private final ItemStack otherHeadItem;
    private final ItemStack tradeButtonItem;

    private final ItemStack[] traderContents;
    private final ItemStack[] otherContents;

    private final Map<String, Double> traderCurrencies;
    private final Map<String, Double> otherCurrencies;
    private final BiFunction<String, Double, Double> currencyAdjustment;

    private final CloseHandle closeHandle;
    private final TradeHandle tradeHandle;

    private boolean confirmed = false;

    public GUIPairNode(
            PluginMain main,
            ItemStack traderHeadItem,
            ItemStack otherHeadItem,
            ItemStack tradeButtonItem,
            ItemStack[] traderContents,
            ItemStack[] otherContents,
            Map<String, Double> traderCurrencies,
            Map<String, Double> otherCurrencies,
            BiFunction<String, Double, Double> currencyAdjustment,
            CloseHandle closeHandle,
            TradeHandle tradeHandle) {
        this.main = main;
        this.traderHeadItem = traderHeadItem;
        this.otherHeadItem = otherHeadItem;
        this.tradeButtonItem = tradeButtonItem;
        this.traderContents = traderContents;
        this.otherContents = otherContents;
        this.traderCurrencies = traderCurrencies;
        this.otherCurrencies = otherCurrencies;
        this.currencyAdjustment = currencyAdjustment;
        this.closeHandle = closeHandle;
        this.tradeHandle = tradeHandle;
    }

    public void setOtherGUI(GUIPairNode otherGUI) {
        this.otherGUI = otherGUI;
    }

    @Override
    public SmartInventory getGUI() {
        return SmartInventory.builder()
                .id(UUID.randomUUID().toString())
                .title("Trade")
                .provider(this)
                .closeable(true)
                .listener(new InventoryListener<>(InventoryCloseEvent.class, (event) ->
                        closeHandle.onClose(this, event)))
                .listener(new InventoryListener<>(InventoryClickEvent.class, this))
                .size(6, 9)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillRow(2, ClickableItem.empty(border(Material.WHITE_STAINED_GLASS_PANE)));

        contents.fillBorders(ClickableItem.empty(border(Material.BLACK_STAINED_GLASS_PANE)));
        contents.fillColumn(4, ClickableItem.empty(border(Material.BLACK_STAINED_GLASS_PANE)));

        update(player, contents);
    }

    private int toLeftContentIndex(int row, int col) {
        return CONTENTS_COL * (row - 3) + (col - 1);
    }

    private int toRightContentIndex(int row, int col) {
        return CONTENTS_COL * (row - 3) + (col - 5);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        contents.set(0, 0, ClickableItem.empty(traderHeadItem));

        contents.set(0, 8, ClickableItem.empty(otherHeadItem));

        //1,1 -> 1,3  left currencies
        contents.set(1, 2, ClickableItem.from(currencyItem(player, Material.PAPER, traderCurrencies), data -> {
            main.api().getAPI(RealEconomyAPI.class).ifPresent(api -> {
                if (!confirmed) {
                    if (traderCurrencies.size() > 0) {
                        GUIAmountSelector selector = new GUIAmountSelector(main,
                                "Price?", // TODO translate?
                                traderCurrencies,
                                (selectorGUI) -> {
                                    // adjust the price depending on trader's account balance
                                    new HashMap<>(traderCurrencies).forEach((curr, amount) -> {
                                        traderCurrencies.put(curr, currencyAdjustment.apply(curr, amount));
                                    });
                                    selectorGUI.detach(player);
                                    getGUI().open(player);
                                });
                        getGUI().detach(player);
                        selector.getGUI().open(player);
                    } else {
                        main.lang().sendMessage(BukkitWrapper.player(player), TradeGUILangs.GUI_Currency_NotEnabled);
                    }
                }
            });
        }));

        //1,6  right currency
        contents.set(1, 6, ClickableItem.empty(currencyItem(player, Material.NETHER_STAR, otherCurrencies)));

        //3,1 -> 4,3  left contents
        contents.applyRect(LEFT_FROM.getRow(),
                LEFT_FROM.getColumn(),
                LEFT_FROM.getRow() + CONTENTS_ROW - 1,
                LEFT_FROM.getColumn() + CONTENTS_COL - 1,
                (row, col) -> {
                    ItemStack itemStack = traderContents[toLeftContentIndex(row, col)];
                    itemStack = itemStack == null ? new ItemStack(Material.AIR) : itemStack;
                    contents.set(row, col, ClickableItem.empty(itemStack));
                });
        contents.applyRect(LEFT_FROM.getRow(),
                LEFT_FROM.getColumn(),
                LEFT_FROM.getRow() + CONTENTS_ROW - 1,
                LEFT_FROM.getColumn() + CONTENTS_COL - 1,
                (row, col) -> contents.setEditable(SlotPos.of(row, col), true));

        //3,5 -> 4,7  right contents
        contents.applyRect(RIGHT_FROM.getRow(),
                RIGHT_FROM.getColumn(),
                RIGHT_FROM.getRow() + CONTENTS_ROW - 1,
                RIGHT_FROM.getColumn() + CONTENTS_COL - 1,
                (row, col) -> {
                    ItemStack itemStack = otherContents[toRightContentIndex(row, col)];
                    itemStack = itemStack == null ? new ItemStack(Material.AIR) : itemStack;
                    contents.set(row, col, ClickableItem.empty(itemStack));
                });

        // trade button
        contents.set(3, 4, ClickableItem.from(tradeButtonItem, data ->
                tradeHandle.onTrade(this, data)));

        // left confirm button
        contents.set(5, 0, ClickableItem.from(confirmItem(player, confirmed, true), data -> {
            if (!confirmed)
                confirmed = true;
        }));

        // right confirm button
        contents.set(5, 8, ClickableItem.empty(confirmItem(player, otherGUI.confirmed, false)));
    }

    private static boolean withInRange(int slot) {
        int row = slot / 9;
        int col = slot % 9;

        return LEFT_FROM.getRow() <= row && row < LEFT_FROM.getRow() + CONTENTS_ROW
                && LEFT_FROM.getColumn() <= col && col < LEFT_FROM.getColumn() + CONTENTS_COL;
    }

    @Override
    public void accept(InventoryClickEvent event) {
        if (confirmed) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != InventoryAction.PLACE_ALL
                && event.getAction() != InventoryAction.PICKUP_ALL) {
            event.setCancelled(true);
            return;
        }

        if (!confirmed && withInRange(event.getSlot())) {
            int index = toLeftContentIndex(event.getSlot() / 9, event.getSlot() % 9);
            Optional.of(event)
                    .map(InventoryClickEvent::getCursor)
                    .map(ItemStack::clone)
                    .ifPresent(itemStack -> traderContents[index] = itemStack);
        }
    }

    private ItemStack currencyItem(Player player, Material material, Map<String, Double> currencies) {
        ItemStack itemStack = new ItemStack(material);
        InventoryUtil.parseFirstToItemTitle(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_Currency_Title,
                itemStack);

        InventoryUtil.parseToItemLores(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_Currency_Line,
                itemStack,
                true);

        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        currencies.forEach((currency, value) -> {
            if (value <= 0.0)
                return;

            lore.add(ChatColor.translateAlternateColorCodes('&',
                    main.lang().parseFirst(BukkitWrapper.player(player),
                            TradeGUILangs.GUI_Currency_Format, (sen, langman) ->
                                    langman.addDouble(value).addString(currency))));
        });
        lore.add("");
        lore.add(main.lang().parseFirst(BukkitWrapper.player(player), TradeGUILangs.GUI_Currency_ClickToEdit));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    private ItemStack confirmItem(Player player, boolean confirm, boolean trader) {
        ILang title_confirm = trader ? TradeGUILangs.GUI_Confirm_Trader_Title : TradeGUILangs.GUI_Confirm_Other_Title;
        ILang lore_confirm = trader ? TradeGUILangs.GUI_Confirm_Trader_Lore : TradeGUILangs.GUI_Confirm_Other_Lore;
        ILang title_confirmed = trader ? TradeGUILangs.GUI_Confirmed_Trader_Title : TradeGUILangs.GUI_Confirmed_Other_Title;
        ILang lore_confirmed = trader ? TradeGUILangs.GUI_Confirmed_Trader_Lore : TradeGUILangs.GUI_Confirmed_Other_Lore;

        ItemStack itemStack = new ItemStack(confirm ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK);
        InventoryUtil.parseFirstToItemTitle(main.lang(),
                BukkitWrapper.player(player),
                confirm ? title_confirmed : title_confirm,
                itemStack);
        InventoryUtil.parseToItemLores(main.lang(),
                BukkitWrapper.player(player),
                confirm ? lore_confirmed : lore_confirm,
                itemStack,
                true);

        return itemStack;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public interface CloseHandle {
        void onClose(GUIPairNode node, InventoryCloseEvent event);
    }

    public interface TradeHandle {
        void onTrade(GUIPairNode node, ItemClickData data);
    }
}
