package io.github.wysohn.tradegui.manager.gui;

import io.github.wysohn.tradegui.api.SmartInvAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public abstract class AbstractGUI extends SmartInvAPI.GUI {
    protected static ItemStack border(Material material) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = Optional.of(itemStack)
                .map(ItemStack::getItemMeta)
                .orElseGet(() -> Bukkit.getItemFactory().getItemMeta(material));
        meta.setDisplayName(ChatColor.WHITE.toString());
        meta.setLore(null);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
