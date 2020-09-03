package io.github.wysohn.tradegui.manager.trade;

import io.github.wysohn.rapidframework2.core.interfaces.IPluginObject;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.tradegui.manager.gui.trade.GUIPair;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

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
        currentTrades.forEach((uuid, guiPair) -> guiPair.stopNow());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Optional.of(event.getPlayer().getUniqueId())
                .flatMap(uuid -> Optional.ofNullable(currentTrades.remove(uuid)))
                .ifPresent(GUIPair::stopNow);
    }

    public boolean isTrading(ITrader iTrader) {
        return Optional.ofNullable(iTrader)
                .map(IPluginObject::getUuid)
                .map(currentTrades::containsKey)
                .orElse(false);
    }

    public boolean startTrade(ITrader initiated, ITrader accepted, Consumer<Boolean> fnResult) {
        if (isTrading(initiated) || isTrading(accepted))
            return false;

        GUIPair pair = new GUIPair(main(), initiated, accepted, (context) -> {
            currentTrades.remove(initiated.getUuid());
            currentTrades.remove(accepted.getUuid());

            fnResult.accept(context.isTrader1Ready() && context.isTrader2Ready());
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
