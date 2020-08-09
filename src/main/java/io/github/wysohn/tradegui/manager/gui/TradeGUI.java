package io.github.wysohn.tradegui.manager.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.SlotPos;
import io.github.wysohn.rapidframework2.bukkit.main.objects.BukkitWrapper;
import io.github.wysohn.rapidframework2.bukkit.utils.InventoryUtil;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.lang.Lang;
import io.github.wysohn.tradegui.api.SmartInvAPI;
import io.github.wysohn.tradegui.main.TradeGUILangs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import util.Validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class TradeGUI extends SmartInvAPI.GUI {
    private final PluginMain main;
    private final TradeGUI otherGUI;

    private Function<ItemStack, ItemStack> traderItemDecorator;
    private Function<ItemStack, ItemStack> otherItemDecorator;
    private Consumer<SmartInventory> updateHandle;
    private Consumer<SmartInventory> confirmHandle;
    private Consumer<SmartInventory> tradeHandle;

    private Map<String, Double> currencies = new LinkedHashMap<>();
    private boolean confirmed = false;

    public TradeGUI(PluginMain main, TradeGUI otherGUI) {
        this.main = main;
        this.otherGUI = otherGUI;
    }

    @Override
    protected SmartInventory init() {
        return SmartInventory.builder()
                .id("Trade")
                .provider(this)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Validation.assertNotNull(updateHandle);

        contents.fillRow(2, ClickableItem.empty(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)));

        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        contents.fillColumn(4, ClickableItem.empty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        contents.set(0, 0, ClickableItem.empty(Optional.of(Material.PLAYER_HEAD)
                .map(ItemStack::new)
                .filter(itemStack -> traderItemDecorator != null)
                .map(traderItemDecorator)
                .orElseGet(() -> new ItemStack(Material.PLAYER_HEAD))));

        contents.set(0, 8, ClickableItem.empty(Optional.of(Material.PLAYER_HEAD)
                .map(ItemStack::new)
                .filter(itemStack -> otherItemDecorator != null)
                .map(otherItemDecorator)
                .orElseGet(() -> new ItemStack(Material.PLAYER_HEAD))));

        //1,1 -> 1,3  left currencies
        contents.set(1, 2, ClickableItem.from(new ItemStack(Material.PAPER), data -> {
            //TODO open numeric value selector
        }));

        //1,6  right currency
        contents.set(1, 6, ClickableItem.empty(new ItemStack(Material.NETHER_STAR)));

        //3,1 -> 4,3  left contents
        contents.applyRect(3, 1, 4, 3, (row, col) ->
                contents.set(row, col, ClickableItem.from(new ItemStack(Material.AIR), data ->
                        updateHandle.accept(getGUI()))));
        contents.applyRect(3, 1, 4, 3, (row, col) -> contents.setEditable(SlotPos.of(row, col), true));

        //3,5 -> 4,7  right contents
        contents.applyRect(3, 5, 4, 7, (row, col) ->
                contents.set(row, col, ClickableItem.empty(new ItemStack(Material.AIR))));

        // trade button
        contents.set(3, 4, ClickableItem.from(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), data ->
                tradeHandle.accept(getGUI())));

        // left confirm button
        contents.set(5, 0, ClickableItem.from(confirmItem(player, confirmed, true), data -> {
            if (!confirmed) {
                confirmed = true;
                confirmHandle.accept(getGUI());
            }
        }));

        // right confirm button
        contents.set(5, 8, ClickableItem.empty(confirmItem(player, otherGUI.confirmed, false)));
    }

    private ItemStack confirmItem(Player player, boolean confirm, boolean trader) {
        Lang title_confirm = trader ? TradeGUILangs.GUI_Confirm_Trader_Title : TradeGUILangs.GUI_Confirm_Other_Title;
        Lang lore_confirm = trader ? TradeGUILangs.GUI_Confirm_Trader_Lore : TradeGUILangs.GUI_Confirm_Other_Lore;
        Lang title_confirmed = trader ? TradeGUILangs.GUI_Confirmed_Trader_Title : TradeGUILangs.GUI_Confirmed_Other_Title;
        Lang lore_confirmed = trader ? TradeGUILangs.GUI_Confirmed_Trader_Lore : TradeGUILangs.GUI_Confirmed_Other_Lore;

        ItemStack itemStack = new ItemStack(confirmed ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK);
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
}
