package io.github.wysohn.tradegui.manager;

import io.github.wysohn.rapidframework3.core.api.ManagerExternalAPI;
import io.github.wysohn.rapidframework3.core.main.Mediator;
import io.github.wysohn.tradegui.api.economy.RealEconomyAPI;
import io.github.wysohn.tradegui.api.economy.VaultAPI;
import io.github.wysohn.tradegui.manager.trade.ITradeContent;
import io.github.wysohn.tradegui.manager.trade.ITrader;
import io.github.wysohn.tradegui.manager.user.User;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class EconomyMediator extends Mediator implements IEconomyProvider {
    @Inject
    private ManagerExternalAPI api;

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    private IEconomyProvider getEconomyProvider(){
        Optional<RealEconomyAPI> optRealEco = api.getAPI(RealEconomyAPI.class);
        Optional<VaultAPI> optVault = api.getAPI(VaultAPI.class);

        if(optRealEco.isPresent()){
            return optRealEco.map(IEconomyProvider.class::cast).orElseThrow(RuntimeException::new);
        } else if(optVault.isPresent()){
            return optVault.map(IEconomyProvider.class::cast).orElseThrow(RuntimeException::new);
        } else {
            throw new RuntimeException("No economy provider found.");
        }
    }

    @Override
    public List<String> getCurrencies() {
        return getEconomyProvider().getCurrencies();
    }

    @Override
    public double balance(UUID userUuid, String currencyName) {
        return getEconomyProvider().balance(userUuid, currencyName);
    }

    @Override
    public boolean withdraw(UUID userUuid, String currencyName, double amount) {
        return getEconomyProvider().withdraw(userUuid, currencyName, amount);
    }

    @Override
    public boolean deposit(UUID userUuid, String currencyName, double amount) {
        return getEconomyProvider().deposit(userUuid, currencyName, amount);
    }

    public TradingCurrency toTradingContent(UUID owner, String currencyName, double amount) {
        return new TradingCurrency(owner, currencyName, amount);
    }

    /**
     * This class is served as a 'transaction order,' so until the visit(ITrader) is invoked,
     * no transaction will be made. When visit(ITrader) is indeed invoked, it will withdraw
     * the amount from the original owner, and deposit the same amount to the given ITrader.
     */
    public class TradingCurrency implements ITradeContent {
        private final UUID originalOwner;
        private final String currency;
        private final double amount;

        public TradingCurrency(UUID originalOwner, String currency, double amount) {
            this.originalOwner = originalOwner;
            this.currency = currency;
            this.amount = amount;
        }

        @Override
        public void visit(ITrader trader) {
            if (trader instanceof User) {
                //TODO add recovery process for transaction failure
                Player player = ((User) trader).getSender();
                if (!withdraw(originalOwner, currency, amount))
                    throw new RuntimeException(String.format("Withdraw failure %s %s %f. Must check before transaction!",
                            originalOwner, currency, amount));
                deposit(player.getUniqueId(), currency, amount);
            } else {
                throw new RuntimeException("Undefined trader " + trader + ". Currency failed: " + currency + " " + amount);
            }
        }
    }
}