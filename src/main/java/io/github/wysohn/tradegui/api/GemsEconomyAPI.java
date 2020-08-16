package io.github.wysohn.tradegui.api;

import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.api.ExternalAPI;
import io.github.wysohn.tradegui.manager.trade.ITradeContent;
import io.github.wysohn.tradegui.manager.trade.ITrader;
import io.github.wysohn.tradegui.manager.user.User;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.AccountManager;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.currency.CurrencyManager;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class GemsEconomyAPI extends ExternalAPI {
    private CurrencyManager currencyManager;
    private AccountManager accountManager;

    public GemsEconomyAPI(PluginMain main, String pluginName) {
        super(main, pluginName);
    }

    @Override
    public void enable() throws Exception {
        currencyManager = GemsEconomy.getInstance().getCurrencyManager();
        accountManager = GemsEconomy.getInstance().getAccountManager();
    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    public boolean hasCurrency(String currencyName) {
        return currencyManager.getCurrency(currencyName) != null;
    }

    public boolean deposit(UUID uuid, Currency currency, double amount) {
        return accountManager.getAccount(uuid).deposit(currency, amount);
    }

    public boolean deposit(UUID uuid, String currencyName, double amount) {
        Currency currency = currencyManager.getCurrency(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return deposit(uuid, currency, amount);
    }

    public boolean withdraw(UUID uuid, Currency currency, double amount) {
        return accountManager.getAccount(uuid).withdraw(currency, amount);
    }

    public boolean withdraw(UUID uuid, String currencyName, double amount) {
        Currency currency = currencyManager.getCurrency(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return withdraw(uuid, currency, amount);
    }

    public TradingCurrency toTradingMaterial(String currencyName, double amount) {
        return Optional.of(GemsEconomy.getInstance())
                .map(GemsEconomy::getCurrencyManager)
                .map(currencyManager -> currencyManager.getCurrency(currencyName))
                .map(currency -> new TradingCurrency(currency, amount))
                .orElse(null);
    }

    public class TradingCurrency implements ITradeContent {
        private final Currency currency;
        private final double amount;

        public TradingCurrency(Currency currency, double amount) {
            this.currency = currency;
            this.amount = amount;
        }

        @Override
        public void visit(ITrader trader) {
            if (trader instanceof User) {
                Player player = ((User) trader).getSender();
                deposit(player.getUniqueId(), currency, amount);
            } else {
                throw new RuntimeException("Undefined trader " + trader + ". Currency failed: " + currency + " " + amount);
            }
        }
    }
}
