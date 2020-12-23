package io.github.wysohn.tradegui.manager;

import java.util.List;
import java.util.UUID;

public interface IEconomyProvider {
    List<String> getCurrencies();

    double balance(UUID userUuid, String currencyName);

    boolean withdraw(UUID userUuid, String currencyName, double amount);

    boolean deposit(UUID userUuid, String currencyName, double amount);
}
