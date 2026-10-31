package nerd.amara;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
                            MinecraftClient.getInstance().execute(() -> {
                                MinecraftClient.getInstance().setScreen(AutoConfig.getConfigScreen(ModConfig.class, MinecraftClient.getInstance().currentScreen).get());
                            });
                            return 1;
                        }))
                .then(literal("kit")
                        .executes(context -> {
                            InventoryChecker.checkInventory(MinecraftClient.getInstance(), true);
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
        return MinecraftClient.getInstance().player.getName().getString();
    }

    private static void handleShowTier(String name) {
        if (name == null || name.isBlank()) {
            sendSimpleError(Text.translatable("francetiers_tagger.command.error.specify_name_show"));
            return;
        }
        RequestManager.fetchPlayerInfo(name,
                (PlayerInfo info) -> {
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance().setScreen(new TierScreen(info));
                    });
                },
                (Http.Status status) -> sendTwoTone(Text.translatable("francetiers_tagger.command.error.prefix", name), Formatting.RED, describeError(status)));
    }

    private static void handleRefresh(String name) {
        if (name == null || name.isBlank()) {
            sendSimpleError(Text.translatable("francetiers_tagger.command.error.specify_name_refresh"));
            return;
        }
        RequestManager.forcePlayerInfoRefresh(name,
                (PlayerInfo info) -> {
                    sendTwoTone(Text.translatable("francetiers_tagger.command.refresh.success"), Formatting.GREEN, Text.literal(name));
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        String newSuffix = ShowedTier.showed_tier(info);
                        TierSuffixCache.putSuffix(name, newSuffix);
                        if (client.world != null) {
                            for (PlayerEntity player : client.world.getPlayers()) {
                                if (player.getName().getString().equalsIgnoreCase(name)) {
                                    ((TierModifier) player).setSuffix(newSuffix);
                                }
                            }
                        }
                        for (DisplayEntity.TextDisplayEntity td : Francetiers_tagger.TextDisplayList) {
                            Entity vehicle = td.getVehicle();
                            if (vehicle instanceof PlayerEntity player && player.getName().getString().equalsIgnoreCase(name)) {
                                Francetiers_tagger.setTextInTextDisplay(info, td, name);
                            }
                        }
                        client.setScreen(new TierScreen(info));
                    });
                },
                (Http.Status status) -> sendTwoTone(Text.translatable("francetiers_tagger.command.error.prefix", name), Formatting.RED, describeError(status)),
                (Long remainingSeconds) -> sendTwoTone(Text.translatable("francetiers_tagger.command.refresh.cooldown", remainingSeconds), Formatting.YELLOW, Text.literal(name)));
    }

    private static void sendMessage(Text text) {
        MinecraftClient.getInstance().player.sendMessage(text, false);
    }

    private static void sendSimpleError(Text message) {
        sendMessage(message.copy().styled(s -> s.withColor(Formatting.RED)));
    }

    private static void sendTwoTone(Text prefix, Formatting prefixColor, Text detail) {
        sendMessage(prefix.copy().styled(s -> s.withColor(prefixColor))
                .append(Text.literal(" "))
                .append(detail.copy().styled(s -> s.withColor(Formatting.WHITE))));
    }

    private static Text describeError(Http.Status status) {
        return switch (status) {
            case NOT_FOUND -> Text.translatable("francetiers_tagger.error.not_found");
            case SERVER_ERROR -> Text.translatable("francetiers_tagger.error.server");
            case NETWORK_ERROR -> Text.translatable("francetiers_tagger.error.network");
            case PARSE_ERROR -> Text.translatable("francetiers_tagger.error.parse");
            default -> Text.translatable("francetiers_tagger.error.unknown");
        };
    }

    private static SuggestionProvider<FabricClientCommandSource> playerNameSuggester() {
        return (context, builder) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() == null) {
                return builder.buildFuture();
            }

            Collection<PlayerListEntry> players = client.getNetworkHandler().getPlayerList();
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
