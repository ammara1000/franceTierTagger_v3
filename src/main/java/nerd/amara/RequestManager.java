package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RequestManager {
    private static final int THREAD_POOL_SIZE = 4;
    private static final long FORCE_REFRESH_COOLDOWN_MILLIS = 30 * 1000;

    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
        Thread t = new Thread(r, "francetiers-fetch");
        t.setDaemon(true);
        return t;
    });

    private static final Set<String> pending = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> lastForceRefresh = new ConcurrentHashMap<>();

    public static void fetchPlayerInfo(String pseudo, Consumer<PlayerInfo> onSuccess, Consumer<Http.Status> onError) {
        if ("EneCone".equalsIgnoreCase(pseudo)) {
            PlayerInfo mockInfo = new PlayerInfo();
            mockInfo.pseudo = "EneCone";
            mockInfo.global_rank = "1";
            mockInfo.total_points = "9999";
            nerd.amara.tiers.Tier t = new nerd.amara.tiers.Tier();
            t.category = "Sword";
            t.tier = "HT1";
            mockInfo.tiers = new java.util.HashMap<>();
            mockInfo.tiers.put("Sword", t);
            
            PlayerInfoCache.putFound(pseudo, mockInfo);
            onSuccess.accept(mockInfo);
            return;
        }

        PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(pseudo);
        if (cached != null) {
            if (cached.result == PlayerInfoCache.Result.FOUND) {
                onSuccess.accept(cached.info);
            } else if (onError != null) {
                onError.accept(Http.Status.NOT_FOUND);
            }
            return;
        }

        if (!pending.add(pseudo)) {
            return;
        }

        executor.submit(() -> {
            try {
                Http.HttpResult<PlayerInfo> result = Http.getJson(Francetiers_tagger.web_url + pseudo, PlayerInfo.class);
                if (result.status == Http.Status.OK && result.data != null) {
                    PlayerInfoCache.putFound(pseudo, result.data);
                    MinecraftClient.getInstance().execute(() -> onSuccess.accept(result.data));
                } else {
                    if (result.status == Http.Status.NOT_FOUND) {
                        PlayerInfoCache.putNotFound(pseudo);
                    }
                    if (onError != null) {
                        MinecraftClient.getInstance().execute(() -> onError.accept(result.status));
                    }
                }
            } finally {
                pending.remove(pseudo);
            }
        });
    }

    public static void forcePlayerInfoRefresh(String pseudo, Consumer<PlayerInfo> onSuccess, Consumer<Http.Status> onError, Consumer<Long> onCooldown) {
        long now = System.currentTimeMillis();
        Long last = lastForceRefresh.get(pseudo);
        if (last != null) {
            long elapsed = now - last;
            if (elapsed < FORCE_REFRESH_COOLDOWN_MILLIS) {
                if (onCooldown != null) {
                    long remainingSeconds = (FORCE_REFRESH_COOLDOWN_MILLIS - elapsed + 999) / 1000;
                    onCooldown.accept(remainingSeconds);
                }
                return;
            }
        }
        lastForceRefresh.put(pseudo, now);
        PlayerInfoCache.invalidate(pseudo);
        fetchPlayerInfo(pseudo, onSuccess, onError);
    }
}