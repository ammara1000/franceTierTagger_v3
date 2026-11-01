package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import net.minecraft.client.Minecraft;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class RequestManager {
    private static final int THREAD_POOL_SIZE = 4;
    private static final long FORCE_REFRESH_COOLDOWN_MILLIS = 30 * 1000L;
    private static final Pattern PSEUDO_PATTERN = Pattern.compile("^\\.?\\w{1,16}$");

    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
        Thread t = new Thread(r, "francetiers-fetch");
        t.setDaemon(true);
        return t;
    });

    private static final Map<String, CompletableFuture<Http.HttpResult<PlayerInfo>>> inFlight = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastForceRefresh = new ConcurrentHashMap<>();

    private static void dispatchOnClient(Runnable action) {
        Minecraft client = Minecraft.getInstance();
        if (client.isSameThread()) {
            action.run();
        } else {
            client.execute(action);
        }
    }

    public static void fetchPlayerInfo(String pseudo, Consumer<PlayerInfo> onSuccess, Consumer<Http.Status> onError) {
        if (pseudo == null || !PSEUDO_PATTERN.matcher(pseudo).matches()) {
            if (onError != null) {
                dispatchOnClient(() -> onError.accept(Http.Status.NOT_FOUND));
            }
            return;
        }

        String key = pseudo.toLowerCase(Locale.ROOT);
        PlayerInfoCache.CachedEntry cached = PlayerInfoCache.get(key);
        boolean isExpired = PlayerInfoCache.isExpired(key);

        if (cached != null) {
            if (cached.result == PlayerInfoCache.Result.FOUND) {
                if (onSuccess != null) {
                    dispatchOnClient(() -> onSuccess.accept(cached.info));
                }
            } else if (cached.result == PlayerInfoCache.Result.ERROR) {
                if (onError != null) {
                    dispatchOnClient(() -> onError.accept(cached.errorStatus));
                }
            } else if (onError != null) {
                dispatchOnClient(() -> onError.accept(Http.Status.NOT_FOUND));
            }

            if (!isExpired) {
                return;
            }
        }

        CompletableFuture<Http.HttpResult<PlayerInfo>> future = inFlight.computeIfAbsent(key, k -> {
            CompletableFuture<Http.HttpResult<PlayerInfo>> newFuture = new CompletableFuture<>();
            executor.submit(() -> {
                try {
                    String encodedPseudo = URLEncoder.encode(pseudo, StandardCharsets.UTF_8);
                    Http.HttpResult<PlayerInfo> result = Http.getJson(Francetiers_tagger.web_url + encodedPseudo, PlayerInfo.class);
                    if (result.status == Http.Status.OK && result.data != null) {
                        PlayerInfoCache.putFound(k, result.data);
                    } else if (result.status == Http.Status.NOT_FOUND) {
                        PlayerInfoCache.putNotFound(k);
                    } else {
                        PlayerInfoCache.putError(k, result.status);
                    }
                    newFuture.complete(result);
                } catch (Exception e) {
                    PlayerInfoCache.putError(k, Http.Status.NETWORK_ERROR);
                    newFuture.complete(Http.HttpResult.error(Http.Status.NETWORK_ERROR));
                } finally {
                    inFlight.remove(k);
                }
            });
            return newFuture;
        });

        future.thenAccept(result -> {
            dispatchOnClient(() -> {
                if (result.status == Http.Status.OK && result.data != null) {
                    if (onSuccess != null) {
                        onSuccess.accept(result.data);
                    }
                } else {
                    if (onError != null) {
                        onError.accept(result.status);
                    }
                }
            });
        });
    }

    public static void forcePlayerInfoRefresh(String pseudo, Consumer<PlayerInfo> onSuccess, Consumer<Http.Status> onError, Consumer<Long> onCooldown) {
        if (pseudo == null || !PSEUDO_PATTERN.matcher(pseudo).matches()) {
            if (onError != null) {
                dispatchOnClient(() -> onError.accept(Http.Status.NOT_FOUND));
            }
            return;
        }

        String key = pseudo.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        Long last = lastForceRefresh.get(key);
        if (last != null) {
            long elapsed = now - last;
            if (elapsed < FORCE_REFRESH_COOLDOWN_MILLIS) {
                if (onCooldown != null) {
                    long remainingSeconds = (FORCE_REFRESH_COOLDOWN_MILLIS - elapsed + 999) / 1000L;
                    dispatchOnClient(() -> onCooldown.accept(remainingSeconds));
                }
                return;
            }
        }
        lastForceRefresh.put(key, now);
        PlayerInfoCache.invalidate(key);
        fetchPlayerInfo(pseudo, onSuccess, onError);
    }
}
