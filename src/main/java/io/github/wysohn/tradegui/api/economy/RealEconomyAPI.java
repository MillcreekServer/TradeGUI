package io.github.wysohn.tradegui.api.economy;

import io.github.wysohn.rapidframework3.core.api.ExternalAPI;
import io.github.wysohn.rapidframework3.core.caching.CachedElement;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import io.github.wysohn.realeconomy.main.RealEconomy;
import io.github.wysohn.realeconomy.manager.user.UserManager;
import io.github.wysohn.realeconomy.manager.currency.CurrencyManager;
import io.github.wysohn.realeconomy.manager.currency.Currency;

import io.github.wysohn.tradegui.manager.EconomyMediator;
import io.github.wysohn.tradegui.manager.IEconomyProvider;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import java.lang.ref.Reference;
import java.util.*;
import java.util.stream.Collectors;

public class RealEconomyAPI extends ExternalAPI implements IEconomyProvider {
    @Inject
    private EconomyMediator mediator;

    private RealEconomy plugin;
    private CurrencyManager currencyManager;
    private UserManager userManager;

    public RealEconomyAPI(PluginMain main, String pluginName) {
        super(main, pluginName);
    }

    @Override
    public void enable() throws Exception {
        plugin = (RealEconomy) Bukkit.getPluginManager().getPlugin(getPluginName());
        currencyManager = plugin.getMain().getManager(CurrencyManager.class).orElseThrow(RuntimeException::new);
        userManager = plugin.getMain().getManager(UserManager.class).orElseThrow(RuntimeException::new);
    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    public boolean hasCurrency(String currencyName) {
        return currencyManager.get(currencyName).isPresent();
    }

    public List<String> getCurrencies() {
        return currencyManager.keySet().stream()
                .map(currencyManager::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Reference::get)
                .filter(Objects::nonNull)
                .map(CachedElement::getStringKey)
                .collect(Collectors.toList());
    }

    private Currency currencyFromName(String currencyName){
        return currencyManager.get(currencyName)
                .map(Reference::get)
                .orElse(null);
    }

    private boolean deposit(UUID uuid, Currency currency, double amount) {
        return userManager.get(uuid)
                .map(Reference::get)
                .map(account -> account.deposit(amount, currency))
                .orElse(false);
    }

    public boolean deposit(UUID uuid, String currencyName, double amount) {
        Currency currency = currencyFromName(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return deposit(uuid, currency, amount);
    }

    private boolean withdraw(UUID uuid, Currency currency, double amount) {
        return userManager.get(uuid)
                .map(Reference::get)
                .map(account -> account.withdraw(amount, currency))
                .orElse(false);
    }

    public boolean withdraw(UUID uuid, String currencyName, double amount) {
        Currency currency = currencyFromName(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return withdraw(uuid, currency, amount);
    }

    private double balance(UUID uuid, Currency currency) {
        return userManager.get(uuid)
                .map(Reference::get)
                .map(account -> account.balance(currency).doubleValue())
                .orElse(0.0);
    }

    public double balance(UUID uuid, String currencyName) {
        Currency currency = currencyFromName(currencyName);
        if (currency == null)
            throw new RuntimeException("currency " + currencyName + " did not exist.");

        return balance(uuid, currency);
    }
}
