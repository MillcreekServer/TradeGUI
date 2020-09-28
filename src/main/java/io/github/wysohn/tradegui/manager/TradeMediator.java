package io.github.wysohn.tradegui.manager;

import io.github.wysohn.rapidframework3.core.language.ManagerLanguage;
import io.github.wysohn.rapidframework3.core.main.Mediator;
import io.github.wysohn.rapidframework3.interfaces.plugin.ITaskSupervisor;
import io.github.wysohn.rapidframework3.utils.OfferScheduler;
import io.github.wysohn.tradegui.main.TradeGUILangs;
import io.github.wysohn.tradegui.manager.trade.TradingManager;
import io.github.wysohn.tradegui.manager.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class TradeMediator extends Mediator {
    private final ITaskSupervisor task;
    private final ManagerLanguage lang;
    private final TradingManager tradingManager;
    private final OfferScheduler offerScheduler;

    @Inject
    public TradeMediator(
            ITaskSupervisor task,
            ManagerLanguage lang,
            TradingManager tradingManager, OfferScheduler offerScheduler) {
        this.task = task;
        this.lang = lang;
        this.tradingManager = tradingManager;
        this.offerScheduler = offerScheduler;
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

    public boolean requestTrade(User user, User target, Consumer<Boolean> fnResult) {
        if (tradingManager.isTrading(user) || tradingManager.isTrading(target))
            return false;

        return offerScheduler.sendOffer(target.getKey(), millis -> {
            lang.sendMessage(target, TradeGUILangs.Trade_Request_TimeLeft, (sen, man) ->
                    man.addInteger((int) (millis / 1000L)));
        }, () -> {
            if (!user.isOnline() || !target.isOnline())
                return;

            if (!tradingManager.startTrade(user, target, fnResult)) {
                lang.sendMessage(target, TradeGUILangs.Trade_Request_AlreadyTrading);
            }
        }, () -> {
            lang.sendMessage(target, TradeGUILangs.Trade_Request_Timeout);
        }, WAITING_MILLIS, WAITING_MILLIS / 2L, WAITING_MILLIS / 4L, 5000L, 4000L, 3000L, 2000L, 1000L);
    }

    public boolean acceptTrade(User user) {
        return offerScheduler.acceptOffer(user.getKey());
    }

    public boolean denyTrade(User user) {
        return offerScheduler.declineOffer(user.getKey());
    }

    public static final long WAITING_MILLIS = 60 * 1000L;
}
