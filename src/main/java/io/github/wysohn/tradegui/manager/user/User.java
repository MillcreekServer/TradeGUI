package io.github.wysohn.tradegui.manager.user;

import fr.minuskube.inv.SmartInventory;
import io.github.wysohn.rapidframework2.bukkit.main.objects.BukkitPlayer;
import io.github.wysohn.tradegui.manager.trade.ITradeContent;
import io.github.wysohn.tradegui.manager.trade.ITrader;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class User extends BukkitPlayer implements ITrader {
    private ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);

    private User() {
        super(null);
    }

    public User(UUID key) {
        super(key);
    }

    @Override
    public ItemStack getHeadItem() {
        ItemMeta meta = headItem.getItemMeta();
        Optional.ofNullable(meta)
                .filter(SkullMeta.class::isInstance)
                .map(SkullMeta.class::cast)
                .ifPresent(skullMeta -> skullMeta.setOwningPlayer(sender));
        headItem.setItemMeta(meta);
        return headItem;
    }

    @Override
    public void openTradeGUI(SmartInventory inventory) {
        inventory.open(sender);
    }

    @Override
    public void closeTradeGUI(SmartInventory inventory) {
        inventory.close(sender);
    }

    @Override
    public void give(Collection<ITradeContent> contents) {
        contents.forEach(content -> content.visit(this));
    }
}
