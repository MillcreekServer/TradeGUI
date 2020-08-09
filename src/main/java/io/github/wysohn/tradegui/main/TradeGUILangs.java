package io.github.wysohn.tradegui.main;

import io.github.wysohn.rapidframework2.core.manager.lang.Lang;

public enum TradeGUILangs implements Lang {
    GUI_Confirm_Trader_Title("&dReady"),
    GUI_Confirm_Trader_Lore("&7Confirm your trades.",
            "&7Once confirm, you can press the trade button,",
            "",
            "&cyet you are not allowed to change contents.",
            "&7Simply restart the trade to change contents"),
    GUI_Confirmed_Trader_Title("&dNow ready to trade..."),
    GUI_Confirmed_Trader_Lore("&cyet you are not allowed to change contents.",
            "&7Simply restart the trade to change contents"),

    GUI_Confirm_Other_Title("&cThis user is not ready."),
    GUI_Confirm_Other_Lore("&7The user is still modifying contents.",
            "&cNote that contents change anytime until confirmed.",
            "&7If suspicious, close the trade GUI anytime."),
    GUI_Confirmed_Other_Title("&aConfirmed"),
    GUI_Confirmed_Other_Lore("&7The contents cannot be changed anymore.",
            "&7You are safe to click trade button,",
            "&dor simply close the GUI to end the trade."),
    ;

    private final String[] def;

    TradeGUILangs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
