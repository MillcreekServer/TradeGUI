package io.github.wysohn.tradegui.manager.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.ItemClickData;
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
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GUIPairNode extends SmartInvAPI.GUI {
    private final PluginMain main;
    private GUIPairNode otherGUI;

    private final ItemStack traderHeadItem;
    private final ItemStack otherHeadItem;

    private final ItemStack[] traderContents;
    private final ItemStack[] otherContents;

    private final Map<String, Double> currencies;

    private final CancelHandle cancelHandle;
    private final TradeHandle tradeHandle;

    private boolean confirmed = false;

    public GUIPairNode(PluginMain main,
                       ItemStack traderHeadItem,
                       ItemStack otherHeadItem,
                       ItemStack[] traderContents,
                       ItemStack[] otherContents,
                       Map<String, Double> currencies,
                       CancelHandle cancelHandle,
                       TradeHandle tradeHandle) {
        this.main = main;
        this.traderHeadItem = traderHeadItem;
        this.otherHeadItem = otherHeadItem;
        this.traderContents = traderContents;
        this.otherContents = otherContents;
        this.currencies = currencies;
        this.cancelHandle = cancelHandle;
        this.tradeHandle = tradeHandle;
    }

    public void setOtherGUI(GUIPairNode otherGUI) {
        this.otherGUI = otherGUI;
    }

    @Override
    protected SmartInventory init() {
        return SmartInventory.builder()
                .id("Trade")
                .provider(this)
                .closeable(true)
                .listener(new InventoryListener<>(InventoryCloseEvent.class, cancelHandle))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillRow(2, ClickableItem.empty(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)));

        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        contents.fillColumn(4, ClickableItem.empty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
    }

    private int toLeftContentIndex(int row, int col) {
        return 3 * (row - 3) + (col - 1);
    }

    private int toRightContentIndex(int row, int col) {
        return 3 * (row - 3) + (col - 5);
    }

    private void updateContents(InventoryContents inventoryContents) {
        inventoryContents.applyRect(3, 1, 4, 3, (row, col) -> {
            int index = toLeftContentIndex(row, col);
            traderContents[index] = null;
            inventoryContents.get(row, col)
                    .ifPresent(clickableItem -> traderContents[index] = clickableItem.getItem());
        });
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        contents.set(0, 0, ClickableItem.empty(traderHeadItem));

        contents.set(0, 8, ClickableItem.empty(otherHeadItem));

        //1,1 -> 1,3  left currencies
        contents.set(1, 2, ClickableItem.from(new ItemStack(Material.PAPER), data -> {
            //TODO open numeric value selector
        }));
        //TODO update lore to show currencies

        //1,6  right currency
        contents.set(1, 6, ClickableItem.empty(new ItemStack(Material.NETHER_STAR)));
        //TODO update lore to show currencies

        //3,1 -> 4,3  left contents
        contents.applyRect(3, 1, 4, 3, (row, col) -> {
            ItemStack itemStack = otherContents[toLeftContentIndex(row, col)];
            itemStack = itemStack == null ? new ItemStack(Material.AIR) : itemStack;
            contents.set(row, col, ClickableItem.from(itemStack, (data) -> {
                if (confirmed) {
                    Optional.of(data)
                            .map(ItemClickData::getEvent)
                            .map(Cancellable.class::cast)
                            .ifPresent(cancellable -> cancellable.setCancelled(true));
                    return;
                }

                updateContents(contents);
            }));
        });
        contents.applyRect(3, 1, 4, 3, (row, col) -> contents.setEditable(SlotPos.of(row, col), true));

        //3,5 -> 4,7  right contents
        contents.applyRect(3, 5, 4, 7, (row, col) -> {
            ItemStack itemStack = otherContents[toRightContentIndex(row, col)];
            itemStack = itemStack == null ? new ItemStack(Material.AIR) : itemStack;
            contents.set(row, col, ClickableItem.empty(itemStack));
        });

        // trade button
        contents.set(3, 4, ClickableItem.from(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), data ->
                tradeHandle.accept(this, data)));

        // left confirm button
        contents.set(5, 0, ClickableItem.from(confirmItem(player, confirmed, true), data -> {
            if (!confirmed)
                confirmed = true;
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

    public boolean isConfirmed() {
        return confirmed;
    }

    public interface CancelHandle extends Consumer<InventoryCloseEvent> {

    }

    public interface TradeHandle extends BiConsumer<GUIPairNode, ItemClickData> {

    }
}
