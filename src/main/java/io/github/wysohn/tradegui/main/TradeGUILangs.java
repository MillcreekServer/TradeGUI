package io.github.wysohn.tradegui.main;

import io.github.wysohn.rapidframework3.interfaces.language.ILang;

public enum TradeGUILangs implements ILang {
    GUI_Currency_Title("&dCurrencies"),
    GUI_Currency_Line("&f"),
    GUI_Currency_Format("&6${double} ${string}"),
    GUI_Currency_ClickToEdit("&d[Click to edit]"),
    GUI_Currency_NotEnabled("&cNo Currency exist."),

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

    GUI_Trade_NotReady_Title("&dClick to finalize the trade."),
    GUI_Trade_NotReady_Lore1("&eYou must confirm first."),
    GUI_Trade_NotReady_Lore2("&aYou are now good to go.",
            "",
            "&cWARNING) Once you confirmed,",
            "&cyou cannot change the contents.",
            "&cThis applies to the other too,",
            "so make sure the other user",
            "confirmed before trading."),

    GUI_Trade_Ready_Title("&aTrade finalized."),
    GUI_Trade_Ready_Lore("&eWaiting for the other user...",
            "",
            "&cIf something is wrong,",
            "close the trade GUI immediately."),

    GUI_AmountSel_Currency_Title("&6Currency"),
    GUI_AmountSel_Currency_Lore("&e${string}",
            "",
            "&d[Left/Right click to change]"),

    GUI_AmountSel_Amount_Title("&6Amount"),
    GUI_AmountSel_Amount_Lore("&e${double} ${string}"),

    GUI_AmountSel_Clear_Title("&dClear"),
    GUI_AmountSel_Clear_Lore("&7Clear entered number."),

    GUI_AmountSel_Exit_Title("&dExit"),
    GUI_AmountSel_Exit_Lore("&7Confirm the current value."),

    Command_Request_Desc("&7Request trade with the specified player."),
    Command_Request_Usage("&d/... r <player> &8: &7Offer trade for <player>"),
    Command_Request_Sent("&aRequest sent."),
    Command_Request_Received("&7Player &6${string} &7wants to trade with you.",
            "  &d/trade accept &5- &7 start trading.",
            "  &d/trade deny &5- &7 refuse the trade.",
            "&7This offer will be automatically declined in &6${integer} &7seconds."),

    Command_Accept_Desc("&7Accept and start the trade."),
    Command_Accept_Usage("&d/... a &8: &7Start trading."),

    Command_Deny_Desc("&7Deny the trade request."),
    Command_Deny_Usage("&d/... a &8: &7Deny the trade offer."),

    Trade_Request_AlreadyTrading("&cAnother trade is already under progress."),
    Trade_Request_NoPendings("&cYou have no trade requests."),
    Trade_Request_TimeLeft("&6${integer} seconds &7left."),
    Trade_Request_Timeout("&7Request timeout."),
    Trade_Request_Denied("&7Trade denied."),

    Trade_Result_Success("&aTrade success!"),
    Trade_Result_Cancelled("&7Trade Cancelled."),

    Trade_Result_CurrencyMismatch("&cEither you or the other player does not have enough currency." +
            " &7It's maybe because the balance has changed before the trade ended.");

    private final String[] def;

    TradeGUILangs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
