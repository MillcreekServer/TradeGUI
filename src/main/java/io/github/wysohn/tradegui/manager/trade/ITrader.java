package io.github.wysohn.tradegui.manager.trade;

import fr.minuskube.inv.SmartInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface ITrader {
    ItemStack getHeadItem();

    void openTradeGUI(SmartInventory inventory);

    void give(Collection<ITradeContent> contents);
}
