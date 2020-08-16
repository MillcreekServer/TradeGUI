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

    Command_Request_Desc("&7Request trade with the specified player."),
    Command_Request_Usage("&d/... r <player> &8: &7Offer trade for <player>"),
    Command_Request_Sent("&aRequest sent."),

    Command_Accept_Desc("&7Accept and start the trade."),
    Command_Accept_Usage("&d/... a &8: &7Start trading."),

    Command_Deny_Desc("&7Deny the trade request."),
    Command_Deny_Usage("&d/... a &8: &7Deny the trade offer."),

    Trade_Request_AlreadyTrading("&cAnother trade is already under progress."),
    Trade_Request_NoPendings("&cYou have no trade requests."),
    Trade_Request_TimeLeft("&6${integer} seconds &7left."),
    Trade_Request_Timeout("&7Request timeout."),
    Trade_Request_Denied("&7Trade denied."),
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
