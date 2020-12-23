package io.github.wysohn.tradegui.main;

import io.github.wysohn.rapidframework3.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework3.core.command.SubCommand;
import io.github.wysohn.rapidframework3.core.command.TabCompleters;
import io.github.wysohn.rapidframework3.core.exceptions.InvalidArgumentException;
import io.github.wysohn.rapidframework3.core.inject.module.*;
import io.github.wysohn.rapidframework3.core.language.DefaultLangs;
import io.github.wysohn.rapidframework3.core.main.PluginMainBuilder;
import io.github.wysohn.rapidframework3.core.player.AbstractPlayerWrapper;
import io.github.wysohn.rapidframework3.utils.Pair;
import io.github.wysohn.tradegui.api.economy.RealEconomyAPI;
import io.github.wysohn.tradegui.api.SmartInvAPI;
import io.github.wysohn.tradegui.inject.module.OfferSchedulerModule;
import io.github.wysohn.tradegui.inject.module.WaitingTimeModule;
import io.github.wysohn.tradegui.manager.EconomyMediator;
import io.github.wysohn.tradegui.manager.TradeMediator;
import io.github.wysohn.tradegui.manager.trade.TradingManager;
import io.github.wysohn.tradegui.manager.user.User;
import io.github.wysohn.tradegui.manager.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TradeGUI extends AbstractBukkitPlugin {
    public TradeGUI() {
    }

    public TradeGUI(Server server) {
        super(server);
    }

    @Override
    protected void init(PluginMainBuilder pluginMainBuilder) {
        pluginMainBuilder
                .addModule(new LanguagesModule(TradeGUILangs.values()))
                .addModule(new ExternalAPIModule(Pair.of("RealEconomy", RealEconomyAPI.class),
                        Pair.of("SmartInv", SmartInvAPI.class)))
                .addModule(new ManagerModule(UserManager.class,
                        TradingManager.class))
                .addModule(new MediatorModule(TradeMediator.class,
                        EconomyMediator.class))
                .addModule(new GsonSerializerModule())
                .addModule(new TypeAsserterModule())
                .addModule(new OfferSchedulerModule())
                .addModule(new WaitingTimeModule());
    }

    @Override
    protected void registerCommands(List<SubCommand.Builder> list) {
        list.add(new SubCommand.Builder("request", 1)
                .withAlias("r")
                .withDescription(TradeGUILangs.Command_Request_Desc)
                .addUsage(TradeGUILangs.Command_Request_Usage)
                .addArgumentMapper(0, s -> Optional.ofNullable(Bukkit.getPlayer(s))
                        .orElseThrow(() -> new InvalidArgumentException(DefaultLangs.General_NoSuchPlayer, (sen, man) ->
                                man.addString(s))))
                .addTabCompleter(0, TabCompleters.PLAYER)
                .action((sender, args) -> {
                    User target = args.get(0)
                            .map(Player.class::cast)
                            .map(Entity::getUniqueId)
                            .filter(uuid -> !uuid.equals(sender.getUuid()))
                            .flatMap(this::getUser)
                            .orElse(null);
                    if (target == null)
                        return true;

                    getMain().getMediator(TradeMediator.class).ifPresent(tradeMediator -> {
                        getUser(sender.getUuid()).ifPresent(user -> {
                            if (tradeMediator.requestTrade(user, target, (result) -> {
                                if (result) {
                                    getMain().lang().sendMessage(sender, TradeGUILangs.Trade_Result_Success);
                                    getMain().lang().sendMessage(target, TradeGUILangs.Trade_Result_Success);
                                } else {
                                    getMain().lang().sendMessage(sender, TradeGUILangs.Trade_Result_Cancelled);
                                    getMain().lang().sendMessage(target, TradeGUILangs.Trade_Result_Cancelled);
                                }
                            })) {
                                getMain().lang().sendMessage(user, TradeGUILangs.Command_Request_Sent);

                                getMain().lang().sendMessage(target, DefaultLangs.General_Line);
                                getMain().lang().sendMessage(target, TradeGUILangs.Command_Request_Received, (sen, langman) ->
                                        langman.addString(sender.getDisplayName())
                                                .addInteger((int) (TradeMediator.WAITING_MILLIS / 1000L)));
                                getMain().lang().sendMessage(target, DefaultLangs.General_Line);
                            } else {
                                getMain().lang().sendMessage(user, TradeGUILangs.Trade_Request_AlreadyTrading);
                            }
                        });
                    });
                    return true;
                }));

        list.add(new SubCommand.Builder("accept", 0)
                .withAlias("a")
                .withDescription(TradeGUILangs.Command_Accept_Desc)
                .addUsage(TradeGUILangs.Command_Accept_Usage)
                .action((sender, args) -> {
                    getMain().getMediator(TradeMediator.class).ifPresent(tradeMediator -> {
                        getUser(sender.getUuid()).ifPresent(user -> {
                            if (!tradeMediator.acceptTrade(user)) {
                                getMain().lang().sendMessage(user, TradeGUILangs.Trade_Request_NoPendings);
                            }
                        });
                    });
                    return true;
                }));

        list.add(new SubCommand.Builder("deny", 0)
                .withAlias("d")
                .withDescription(TradeGUILangs.Command_Deny_Desc)
                .addUsage(TradeGUILangs.Command_Deny_Usage)
                .action((sender, args) -> {
                    getMain().getMediator(TradeMediator.class).ifPresent(tradeMediator -> {
                        getUser(sender.getUuid()).ifPresent(user -> {
                            if (tradeMediator.denyTrade(user)) {
                                getMain().lang().sendMessage(user, TradeGUILangs.Trade_Request_Denied);
                            } else {
                                getMain().lang().sendMessage(user, TradeGUILangs.Trade_Request_NoPendings);
                            }
                        });
                    });
                    return true;
                }));
    }

    private Optional<User> getUser(UUID uuid) {
        return getMain().getManager(UserManager.class)
                .flatMap(userManager -> userManager.get(uuid).map(Reference::get));
    }

    @Override
    protected Optional<? extends AbstractPlayerWrapper> getPlayerWrapper(UUID uuid) {
        return getUser(uuid);
    }
}
