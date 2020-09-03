package io.github.wysohn.tradegui.main;

import io.github.wysohn.rapidframework2.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework2.bukkit.main.BukkitPluginBridge;
import io.github.wysohn.rapidframework2.core.interfaces.plugin.IPluginManager;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.command.InvalidArgumentException;
import io.github.wysohn.rapidframework2.core.manager.command.SubCommand;
import io.github.wysohn.rapidframework2.core.manager.command.TabCompleter;
import io.github.wysohn.rapidframework2.core.manager.lang.DefaultLangs;
import io.github.wysohn.tradegui.api.GemsEconomyAPI;
import io.github.wysohn.tradegui.api.SmartInvAPI;
import io.github.wysohn.tradegui.manager.TradeMediator;
import io.github.wysohn.tradegui.manager.trade.TradingManager;
import io.github.wysohn.tradegui.manager.user.User;
import io.github.wysohn.tradegui.manager.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.ref.Reference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class TradeGUIBridge extends BukkitPluginBridge {
    public TradeGUIBridge(AbstractBukkitPlugin bukkit) {
        super(bukkit);
    }

    public TradeGUIBridge(String pluginName,
                          String pluginDescription,
                          String mainCommand,
                          String adminPermission,
                          Logger logger,
                          File dataFolder,
                          IPluginManager iPluginManager,
                          AbstractBukkitPlugin bukkit) {
        super(pluginName, pluginDescription, mainCommand, adminPermission, logger, dataFolder, iPluginManager, bukkit);
    }

    @Override
    protected PluginMain init(PluginMain.Builder builder) {
        return builder
                .addLangs(TradeGUILangs.values())
                .withExternalAPIs("GemsEconomy", GemsEconomyAPI.class)
                .withExternalAPIs("SmartInv", SmartInvAPI.class)
                .withManagers(new UserManager(PluginMain.Manager.NORM_PRIORITY))
                .withManagers(new TradingManager(PluginMain.Manager.NORM_PRIORITY))
                .withMediators(new TradeMediator())
                .build();
    }

    @Override
    protected void registerCommands(List<SubCommand> list) {
        list.add(new SubCommand.Builder(getMain(), "request", 1)
                .withAlias("r")
                .withDescription(TradeGUILangs.Command_Request_Desc)
                .addUsage(TradeGUILangs.Command_Request_Usage)
                .addArgumentMapper(0, s -> Optional.ofNullable(Bukkit.getPlayer(s))
                        .orElseThrow(() -> new InvalidArgumentException(DefaultLangs.General_NoSuchPlayer, (sen, man) ->
                                man.addString(s))))
                .addTabCompleter(0, TabCompleter.PLAYER)
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
                })
                .create());

        list.add(new SubCommand.Builder(getMain(), "accept", 0)
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
                })
                .create());

        list.add(new SubCommand.Builder(getMain(), "deny", 0)
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
                })
                .create());
    }

    private Optional<User> getUser(UUID uuid) {
        return getMain().getManager(UserManager.class)
                .flatMap(userManager -> userManager.get(uuid).map(Reference::get));
    }
}
