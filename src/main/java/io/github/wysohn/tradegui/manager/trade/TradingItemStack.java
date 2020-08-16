package io.github.wysohn.tradegui.manager.trade;

import io.github.wysohn.tradegui.manager.user.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

public class TradingItemStack implements ITradeContent {
    private final ItemStack itemStack;

    public TradingItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public void visit(ITrader trader) {
        if (trader instanceof User) {
            Player player = ((User) trader).getSender();
            Map<Integer, ItemStack> leftOvers = player.getInventory().addItem(itemStack);
            leftOvers.values().stream()
                    .filter(Objects::nonNull)
                    .forEach(val -> player.getWorld().dropItem(player.getLocation(), val));
        } else {
            throw new RuntimeException("Undefined trader " + trader + ". ItemStack failed: " + itemStack);
        }
    }
}
