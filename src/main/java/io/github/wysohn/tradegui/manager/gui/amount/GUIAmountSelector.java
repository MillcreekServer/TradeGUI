package io.github.wysohn.tradegui.manager.gui.amount;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import io.github.wysohn.rapidframework2.bukkit.main.objects.BukkitWrapper;
import io.github.wysohn.rapidframework2.bukkit.utils.InventoryUtil;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.tools.Validation;
import io.github.wysohn.tradegui.main.TradeGUILangs;
import io.github.wysohn.tradegui.manager.gui.AbstractGUI;
import io.github.wysohn.tradegui.util.NumBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class GUIAmountSelector extends AbstractGUI implements Consumer<InventoryCloseEvent> {
    private final Map<String, NumBuilder> numBuilders;
    private final List<String> currencyNames = new ArrayList<>();
    private int currencyIndex = 0;

    private final PluginMain main;
    private final SmartInventory inventory;
    private final Consumer<SmartInventory> callback;

    private final Map<String, Double> currencyMap;

    public GUIAmountSelector(PluginMain main,
                             String title,
                             Map<String, Double> currencyMap,
                             Consumer<SmartInventory> callback) {
        Validation.assertNotNull(currencyMap);
        Validation.validate(currencyMap, list -> list.size() > 0, "Size must be at least 1");
        // first Currency is shown first
        this.currencyMap = currencyMap;
        this.numBuilders = new HashMap<>();
        this.callback = callback;

        currencyMap.forEach((name, amount) -> {
            numBuilders.put(name, new NumBuilder(amount));
            currencyNames.add(name);
        });

        this.main = main;
        this.inventory = SmartInventory.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .provider(this)
                .closeable(true)
                .listener(new InventoryListener<>(InventoryCloseEvent.class, this))
                .size(6, 9)
                .build();
    }

    @Override
    public SmartInventory getGUI() {
        return this.inventory;
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        inventoryContents.fillSquare(0, 53, ClickableItem.empty(border(Material.WHITE_STAINED_GLASS_PANE)));
        inventoryContents.fillBorders(ClickableItem.empty(border(Material.BLACK_STAINED_GLASS_PANE)));

        //2,3 -> 4,5 numpad 1~9
        inventoryContents.applyRect(2, 3, 4, 5, (row, col) -> {
            int index = 3 * (row - 2) + (col - 3);
            inventoryContents.set(row, col, numpadButton(index + 1));
        });

        //2,6 clear nums
        inventoryContents.set(2, 6, ClickableItem.from(clearItem(main, player), (data) ->
                getNumBuilder().clear()));

        //4,6 numpad 0
        inventoryContents.set(42, numpadButton(0));

        //4,7 exit
        inventoryContents.set(4, 7, ClickableItem.from(exitItem(main, player), (data) ->
                main.task().sync(() -> callback.accept(getGUI()))));
    }

    private ClickableItem numpadButton(int digit) {
        ItemStack itemStack = new ItemStack(Material.SUNFLOWER, digit);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(String.valueOf(digit));
        itemStack.setItemMeta(meta);

        return ClickableItem.from(itemStack, (data) -> {
            getNumBuilder().append(digit);
            synchronizeValues();
        });
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
        inventoryContents.set(12, ClickableItem.from(currencyItem(main, player), (data) ->
                currencyIndex = (currencyIndex + 1) % currencyMap.size()));

        inventoryContents.set(13, ClickableItem.empty(amountItem(main, player)));

        //4,2 dot
        inventoryContents.set(38, ClickableItem.from(dotItem(), data ->
                getNumBuilder().toggleDot()));
    }

    @Override
    public void accept(InventoryCloseEvent event) {
        main.task().sync(() -> callback.accept(getGUI()));
    }

    private ItemStack currencyItem(PluginMain main, Player player) {
        ItemStack itemStack = new ItemStack(Material.ACACIA_SIGN);
        InventoryUtil.parseFirstToItemTitle(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Currency_Title,
                itemStack);
        InventoryUtil.parseToItemLores(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Currency_Lore,
                (sen, langman) -> langman.addString(String.valueOf(getCurrencyName())),
                itemStack,
                true);
        return itemStack;
    }

    private ItemStack amountItem(PluginMain main, Player player) {
        ItemStack itemStack = new ItemStack(Material.GOLD_INGOT);
        InventoryUtil.parseFirstToItemTitle(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Amount_Title,
                itemStack);
        InventoryUtil.parseToItemLores(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Amount_Lore,
                (sen, langman) -> langman.addDouble(getNumBuilder().getNum())
                        .addString(String.valueOf(getCurrencyName())),
                itemStack,
                true);
        return itemStack;
    }

    private ItemStack dotItem() {
        ItemStack itemStack = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(".");
        if (getNumBuilder().isDot())
            itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private NumBuilder getNumBuilder() {
        return numBuilders.get(getCurrencyName());
    }

    private String getCurrencyName() {
        return currencyNames.get(currencyIndex);
    }

    private void synchronizeValues() {
        currencyMap.put(getCurrencyName(), getNumBuilder().getNum());
    }

    private ItemStack clearItem(PluginMain main, Player player) {
        ItemStack itemStack = new ItemStack(Material.ARROW);
        InventoryUtil.parseFirstToItemTitle(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Clear_Title,
                itemStack);
        InventoryUtil.parseToItemLores(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Clear_Lore,
                itemStack,
                true);
        return itemStack;
    }

    private ItemStack exitItem(PluginMain main, Player player) {
        ItemStack itemStack = new ItemStack(Material.OAK_DOOR);
        InventoryUtil.parseFirstToItemTitle(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Exit_Title,
                itemStack);
        InventoryUtil.parseToItemLores(main.lang(),
                BukkitWrapper.player(player),
                TradeGUILangs.GUI_AmountSel_Exit_Lore,
                itemStack,
                true);
        return itemStack;
    }
}
