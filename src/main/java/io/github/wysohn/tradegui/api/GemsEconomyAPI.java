package io.github.wysohn.tradegui.api;

import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.api.ExternalAPI;
import io.github.wysohn.tradegui.manager.trade.ITradeContent;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;

import java.util.Optional;

public class GemsEconomyAPI extends ExternalAPI {

    public GemsEconomyAPI(PluginMain main, String pluginName) {
        super(main, pluginName);
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

    public TradingCurrency toTradingMaterial(String currencyName, double amount) {
        return Optional.of(GemsEconomy.getInstance())
                .map(GemsEconomy::getCurrencyManager)
                .map(currencyManager -> currencyManager.getCurrency(currencyName))
                .map(currency -> new TradingCurrency(currency, amount))
                .orElse(null);
    }

    public static class TradingCurrency implements ITradeContent {
        private final Currency currency;
        private final double amount;

        public TradingCurrency(Currency currency, double amount) {
            this.currency = currency;
            this.amount = amount;
        }
    }
}
