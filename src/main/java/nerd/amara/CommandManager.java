package nerd.amara;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import nerd.amara.tiers.PlayerInfo;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandManager {
    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("francetiers")
                .then(argument("name", StringArgumentType.word())
                        .suggests(playerNameSuggester())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            if (name == null || name.isBlank()) {
                                sendSimpleError("Merci d'indiquer un pseudo, ex: /francetiers <pseudo>");
                                return 1;
                            }

                            RequestManager.fetchPlayerInfo(name,
                                    CommandManager::sendTierMessage,
                                    (Http.Status status) -> sendTwoTone("Erreur (" + name + "): ", Formatting.RED, describeError(status)));

                            return 1;
                        }))
                .then(literal("refresh")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(playerNameSuggester())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    if (name == null || name.isBlank()) {
                                        sendSimpleError("Merci d'indiquer un pseudo, ex: /francetiers refresh <pseudo>");
                                        return 1;
                                    }

                                    RequestManager.forcePlayerInfoRefresh(name,
                                            (PlayerInfo info) -> {
                                                sendTwoTone("Tier actualisé pour ", Formatting.GREEN, name);
                                                sendTierMessage(info);
                                            },
                                            (Http.Status status) -> sendTwoTone("Erreur (" + name + "): ", Formatting.RED, describeError(status)),
                                            (Long remainingSeconds) -> sendTwoTone("Attends encore " + remainingSeconds + "s avant de réactualiser ", Formatting.YELLOW, name));

                                    return 1;
                                })))
        );
    }

    private static void sendMessage(Text text) {
        MinecraftClient.getInstance().player.sendMessage(text, false);
    }

    private static void sendSimpleError(String message) {
        sendMessage(Text.literal(message).styled(s -> s.withColor(Formatting.RED)));
    }

    private static void sendTwoTone(String prefix, Formatting prefixColor, String detail) {
        sendMessage(Text.literal(prefix).styled(s -> s.withColor(prefixColor))
                .append(Text.literal(detail).styled(s -> s.withColor(Formatting.WHITE))));
    }

    private static void sendTierMessage(PlayerInfo info) {
        sendMessage(Text.literal(ShowedTier.showed_message(info)).styled(s -> s.withColor(Formatting.WHITE).withFont(new StyleSpriteSource.Font(Identifier.of("frtl", "lol")))));
    }

    private static String describeError(Http.Status status) {
        return switch (status) {
            case NOT_FOUND -> "joueur introuvable";
            case SERVER_ERROR -> "le serveur FranceTiers ne répond pas correctement, réessaie plus tard";
            case NETWORK_ERROR -> "impossible de contacter FranceTiers, vérifie ta connexion";
            case PARSE_ERROR -> "réponse invalide reçue de FranceTiers";
            default -> "erreur inconnue";
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