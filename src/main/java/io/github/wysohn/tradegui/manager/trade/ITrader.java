package io.github.wysohn.tradegui.manager.trade;

import fr.minuskube.inv.SmartInventory;
import io.github.wysohn.rapidframework2.core.interfaces.IPluginObject;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface ITrader extends IPluginObject {
    ItemStack getHeadItem();

    void openTradeGUI(SmartInventory inventory);

    void closeTradeGUI(SmartInventory inventory);

    void give(Collection<ITradeContent> contents);
}
