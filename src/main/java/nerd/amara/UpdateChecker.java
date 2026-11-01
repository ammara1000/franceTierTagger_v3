package nerd.amara;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    private static boolean hasChecked = false;
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/FranceTiersTagger/version";
    private static final String MODRINTH_PAGE = "https://modrinth.com/mod/FranceTiersTagger";

    public static void checkAsync() {
        if (hasChecked) {
            return;
        }
        hasChecked = true;

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (config == null || !config.checkForUpdates) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String currentVersion = getCurrentVersion();
                String mcVersion = getMinecraftVersion();
                String apiUrl = MODRINTH_API;
                if (!mcVersion.isEmpty()) {
                    apiUrl += "?game_versions=%5B%22" + mcVersion + "%22%5D";
                }
                String body = Http.getString(apiUrl, Duration.ofSeconds(5));
                JsonElement root = JsonParser.parseString(body);
                if (!root.isJsonArray()) {
                    return;
                }

                JsonArray versions = root.getAsJsonArray();
                String latestRelease = null;

                for (JsonElement elem : versions) {
                    if (!elem.isJsonObject()) continue;
                    JsonObject obj = elem.getAsJsonObject();
                    String versionType = obj.has("version_type") ? obj.get("version_type").getAsString() : "";
                    if (!"release".equalsIgnoreCase(versionType)) {
                        continue;
                    }

                    if (!mcVersion.isEmpty() && obj.has("game_versions") && obj.get("game_versions").isJsonArray()) {
                        boolean matchesMc = false;
                        for (JsonElement gv : obj.getAsJsonArray("game_versions")) {
                            if (gv.isJsonPrimitive() && gv.getAsString().equals(mcVersion)) {
                                matchesMc = true;
                                break;
                            }
                        }
                        if (!matchesMc) {
                            continue;
                        }
                    }

                    String versionNumber = obj.has("version_number") ? obj.get("version_number").getAsString() : null;
                    if (versionNumber == null || versionNumber.isEmpty()) {
                        continue;
                    }

                    if (latestRelease == null || isNewer(versionNumber, latestRelease)) {
                        latestRelease = versionNumber;
                    }
                }

                if (latestRelease != null && isNewer(latestRelease, currentVersion)) {
                    Minecraft client = Minecraft.getInstance();
                    if (client != null && client.player != null && client.screen == null) {
                        UpdateNotification.trigger(latestRelease, currentVersion, MODRINTH_PAGE);
                    } else {
                        UpdateNotification.setPending(latestRelease, currentVersion, MODRINTH_PAGE);
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    public static String getCurrentVersion() {
        return FabricLoader.getInstance().getModContainer("francetiers_tagger")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("3.1.0");
    }

    public static String getMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("");
    }

    public static boolean isNewer(String remote, String current) {
        if (remote == null || current == null) {
            return false;
        }
        String cleanRemote = remote.replaceAll("^[^0-9]+", "");
        String cleanCurrent = current.replaceAll("^[^0-9]+", "");

        String[] rParts = cleanRemote.split("[.-]");
        String[] cParts = cleanCurrent.split("[.-]");

        int max = Math.max(rParts.length, cParts.length);
        for (int i = 0; i < max; i++) {
            int r = 0;
            int c = 0;
            if (i < rParts.length) {
                try {
                    r = Integer.parseInt(rParts[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }
            if (i < cParts.length) {
                try {
                    c = Integer.parseInt(cParts[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
            }
            if (r > c) return true;
            if (r < c) return false;
        }
        return false;
    }
}
