package io.github.wysohn.tradegui.api.economy;

import io.github.wysohn.rapidframework3.core.api.ExternalAPI;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import io.github.wysohn.tradegui.manager.IEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VaultAPI extends ExternalAPI implements IEconomyProvider {
    private Economy economy;

    public VaultAPI(PluginMain main, String pluginName) {
        super(main, pluginName);
    }

    @Override
    public void enable() throws Exception {
        economy = Optional.ofNullable(Bukkit.getServicesManager())
                .map(servicesManager -> servicesManager.getRegistration(Economy.class))
                .map(RegisteredServiceProvider::getProvider)
                .orElse(null); // Vault plugin exist but Economy is not registered.
    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    @Override
    public List<String> getCurrencies() {
        // vault doesn't support multi-currency
        // empty string
        return Collections.singletonList("");
    }

    @Override
    public double balance(UUID userUuid, String currencyName) {
        if(economy == null)
            throw new RuntimeException("Vault economy is not enabled.");

        return economy.getBalance(Bukkit.getOfflinePlayer(userUuid));
    }

    @Override
    public boolean withdraw(UUID userUuid, String currencyName, double amount) {
        if(economy == null)
            throw new RuntimeException("Vault economy is not enabled.");

        return economy.withdrawPlayer(Bukkit.getOfflinePlayer(userUuid), amount).transactionSuccess();
    }

    @Override
    public boolean deposit(UUID userUuid, String currencyName, double amount) {
        if(economy == null)
            throw new RuntimeException("Vault economy is not enabled.");

        return economy.depositPlayer(Bukkit.getOfflinePlayer(userUuid), amount).transactionSuccess();
    }
}
