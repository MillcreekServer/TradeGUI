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

import java.util.ArrayList;
import java.util.List;
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

    public List<Currency> getCurrencies() {
        return new ArrayList<>(currencyManager.getAll());
    }

    public boolean deposit(UUID uuid, Currency currency, double amount) {
        return Optional.ofNullable(accountManager.get(uuid))
                .map(account -> account.deposit(currency, amount))
                .orElse(false);
    }

    public boolean deposit(UUID uuid, String currencyName, double amount) {
        Currency currency = currencyManager.getCurrency(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return deposit(uuid, currency, amount);
    }

    public boolean withdraw(UUID uuid, Currency currency, double amount) {
        return Optional.ofNullable(accountManager.get(uuid))
                .map(account -> account.withdraw(currency, amount))
                .orElse(false);
    }

    public boolean withdraw(UUID uuid, String currencyName, double amount) {
        Currency currency = currencyManager.getCurrency(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return withdraw(uuid, currency, amount);
    }

    public double balance(UUID uuid, Currency currency) {
        return Optional.ofNullable(accountManager.get(uuid))
                .map(account -> account.getBalance(currency))
                .orElse(0.0);
    }

    public double balance(UUID uuid, String currencyName) {
        return Optional.ofNullable(accountManager.get(uuid))
                .map(account -> account.getBalance(currencyName))
                .orElse(0.0);
    }

    public TradingCurrency toTradingContent(UUID owner, String currencyName, double amount) {
        return Optional.of(GemsEconomy.getInstance())
                .map(GemsEconomy::getCurrencyManager)
                .map(currencyManager -> currencyManager.getCurrency(currencyName))
                .map(currency -> new TradingCurrency(owner, currency, amount))
                .orElse(null);
    }

    /**
     * This class is served as a 'transaction order,' so until the visit(ITrader) is invoked,
     * no transaction will be made. When visit(ITrader) is indeed invoked, it will withdraw
     * the amount from the original owner, and deposit the same amount to the given ITrader.
     */
    public class TradingCurrency implements ITradeContent {
        private final UUID originalOwner;
        private final Currency currency;
        private final double amount;

        public TradingCurrency(UUID originalOwner, Currency currency, double amount) {
            this.originalOwner = originalOwner;
            this.currency = currency;
            this.amount = amount;
        }

        @Override
        public void visit(ITrader trader) {
            if (trader instanceof User) {
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
