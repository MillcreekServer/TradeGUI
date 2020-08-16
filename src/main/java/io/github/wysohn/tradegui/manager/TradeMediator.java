package io.github.wysohn.tradegui.manager;

import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.common.OfferScheduler;
import io.github.wysohn.tradegui.main.TradeGUILangs;
import io.github.wysohn.tradegui.manager.trade.TradingManager;
import io.github.wysohn.tradegui.manager.user.User;

public class TradeMediator extends PluginMain.Mediator {
    private OfferScheduler offerScheduler;

    private TradingManager tradingManager;

    @Override
    public void enable() throws Exception {
        offerScheduler = new OfferScheduler(main().task());
        tradingManager = main().getManager(TradingManager.class).get();
    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    public boolean requestTrade(User user, User target) {
        if (tradingManager.isTrading(user) || tradingManager.isTrading(target))
            return false;

        return offerScheduler.sendOffer(target, millis -> {
            main().lang().sendMessage(target, TradeGUILangs.Trade_Request_TimeLeft, (sen, man) ->
                    man.addInteger((int) (millis / 1000L)));
        }, () -> {
            if (!tradingManager.startTrade(user, target)) {
                main().lang().sendMessage(target, TradeGUILangs.Trade_Request_AlreadyTrading);
            }
        }, () -> {
            main().lang().sendMessage(target, TradeGUILangs.Trade_Request_Timeout);
        });
    }

    public boolean acceptTrade(User user) {
        return offerScheduler.acceptOffer(user);
    }

    public boolean denyTrade(User user) {
        return offerScheduler.declineOffer(user);
    }
}
