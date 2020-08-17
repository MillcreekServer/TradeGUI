package io.github.wysohn.tradegui.manager.trade;

import io.github.wysohn.rapidframework2.core.interfaces.IPluginObject;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.tradegui.manager.gui.GUIPair;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TradingManager extends PluginMain.Manager implements Listener {
    private final Map<UUID, GUIPair> currentTrades = new HashMap<>();

    public TradingManager(int loadPriority) {
        super(loadPriority);
    }

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Optional.of(event.getPlayer().getUniqueId())
                .ifPresent(currentTrades::remove);
    }

    public boolean isTrading(ITrader iTrader) {
        return Optional.ofNullable(iTrader)
                .map(IPluginObject::getUuid)
                .map(currentTrades::containsKey)
                .orElse(false);
    }

    public boolean startTrade(ITrader initiated, ITrader accepted) {
        if (isTrading(initiated) || isTrading(accepted))
            return false;

        GUIPair pair = new GUIPair(main(), initiated, accepted, (context, guiNode) -> {
            currentTrades.remove(initiated.getUuid());
            currentTrades.remove(accepted.getUuid());

            initiated.closeTradeGUI(context.getTrader1GUI().getGUI());
            accepted.closeTradeGUI(context.getTrader2GUI().getGUI());
        });

        currentTrades.put(initiated.getUuid(), pair);
        currentTrades.put(accepted.getUuid(), pair);
        pair.begin();
        return true;
    }

    public GUIPair getCurrentTradeGUI(UUID uuid) {
        return currentTrades.get(uuid);
    }
}
