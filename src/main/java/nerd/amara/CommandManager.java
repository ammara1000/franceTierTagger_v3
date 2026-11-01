package nerd.amara;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandManager {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var francetiersCmd = literal("francetiers")
                .executes(context -> {
                    handleShowTier(ownName());
                    return 1;
                })
                .then(literal("show")
                        .executes(context -> {
                            handleShowTier(ownName());
                            return 1;
                        })
                        .then(argument("name", StringArgumentType.word())
                                .suggests(playerNameSuggester())
                                .executes(context -> {
                                    handleShowTier(StringArgumentType.getString(context, "name"));
                                    return 1;
                                })))
                .then(literal("refresh")
                        .executes(context -> {
                            handleRefresh(ownName());
                            return 1;
                        })
                        .then(argument("name", StringArgumentType.word())
                                .suggests(playerNameSuggester())
                                .executes(context -> {
                                    handleRefresh(StringArgumentType.getString(context, "name"));
                                    return 1;
                                })))
                .then(literal("config")
                        .executes(context -> {
                            Minecraft.getInstance().execute(() -> {
                                Minecraft.getInstance().setScreen(AutoConfig.getConfigScreen(ModConfig.class, Minecraft.getInstance().screen).get());
                            });
                            return 1;
                        }))
                .then(literal("kit")
                        .executes(context -> {
                            InventoryChecker.checkInventory(Minecraft.getInstance(), true);
                            return 1;
                        }))
                .then(literal("update")
                        .executes(context -> {
                            UpdateNotification.openModrinth();
                            return 1;
                        }));

        dispatcher.register(francetiersCmd);
        dispatcher.register(literal("frtl")
                .executes(francetiersCmd.getCommand())
                .redirect(francetiersCmd.build()));
    }

    private static String ownName() {
        return Minecraft.getInstance().player.getName().getString();
    }

    private static void handleShowTier(String name) {
        if (name == null || name.isBlank()) {
            sendSimpleError(Component.translatable("francetiers_tagger.command.error.specify_name_show"));
            return;
        }
        RequestManager.fetchPlayerInfo(name,
                (PlayerInfo info) -> {
                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().setScreen(new TierScreen(info));
                    });
                },
                (Http.Status status) -> sendTwoTone(Component.translatable("francetiers_tagger.command.error.prefix", name), ChatFormatting.RED, describeError(status)));
    }

    private static void handleRefresh(String name) {
        if (name == null || name.isBlank()) {
            sendSimpleError(Component.translatable("francetiers_tagger.command.error.specify_name_refresh"));
            return;
        }
        RequestManager.forcePlayerInfoRefresh(name,
                (PlayerInfo info) -> {
                    sendTwoTone(Component.translatable("francetiers_tagger.command.refresh.success"), ChatFormatting.GREEN, Component.literal(name));
                    Minecraft.getInstance().execute(() -> {
                        Minecraft client = Minecraft.getInstance();
                        String newSuffix = ShowedTier.showed_tier(info);
                        TierSuffixCache.putSuffix(name, newSuffix);
                        if (client.level != null) {
                            for (Player player : client.level.players()) {
                                if (player.getName().getString().equalsIgnoreCase(name)) {
                                    ((TierModifier) player).setSuffix(newSuffix);
                                }
                            }
                        }
                        for (Display.TextDisplay td : Francetiers_tagger.TextDisplayList) {
                            Entity vehicle = td.getVehicle();
                            if (vehicle instanceof Player player && player.getName().getString().equalsIgnoreCase(name)) {
                                Francetiers_tagger.setTextInTextDisplay(info, td, name);
                            }
                        }
                        client.setScreen(new TierScreen(info));
                    });
                },
                (Http.Status status) -> sendTwoTone(Component.translatable("francetiers_tagger.command.error.prefix", name), ChatFormatting.RED, describeError(status)),
                (Long remainingSeconds) -> sendTwoTone(Component.translatable("francetiers_tagger.command.refresh.cooldown", remainingSeconds), ChatFormatting.YELLOW, Component.literal(name)));
    }

    private static void sendMessage(Component text) {
        Minecraft.getInstance().player.displayClientMessage(text, false);
    }

    private static void sendSimpleError(Component message) {
        sendMessage(message.copy().withStyle(s -> s.withColor(ChatFormatting.RED)));
    }

    private static void sendTwoTone(Component prefix, ChatFormatting prefixColor, Component detail) {
        sendMessage(prefix.copy().withStyle(s -> s.withColor(prefixColor))
                .append(Component.literal(" "))
                .append(detail.copy().withStyle(s -> s.withColor(ChatFormatting.WHITE))));
    }

    private static Component describeError(Http.Status status) {
        return switch (status) {
            case NOT_FOUND -> Component.translatable("francetiers_tagger.error.not_found");
            case SERVER_ERROR -> Component.translatable("francetiers_tagger.error.server");
            case NETWORK_ERROR -> Component.translatable("francetiers_tagger.error.network");
            case PARSE_ERROR -> Component.translatable("francetiers_tagger.error.parse");
            default -> Component.translatable("francetiers_tagger.error.unknown");
        };
    }

    private static SuggestionProvider<FabricClientCommandSource> playerNameSuggester() {
        return (context, builder) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() == null) {
                return builder.buildFuture();
            }

            Collection<net.minecraft.client.multiplayer.PlayerInfo> players = client.getConnection().getOnlinePlayers();
            List<String> names = players.stream()
                    .map(entry -> entry.getProfile().name())
                    .collect(Collectors.toList());

            for (String name : names) {
                builder.suggest(name);
            }

            return builder.buildFuture();
        };
    }
}
