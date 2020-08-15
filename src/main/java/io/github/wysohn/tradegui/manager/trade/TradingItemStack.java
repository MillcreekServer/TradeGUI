package io.github.wysohn.tradegui.manager.trade;

import org.bukkit.inventory.ItemStack;

public class TradingItemStack implements ITradeContent {
    private final ItemStack itemStack;

    public TradingItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
