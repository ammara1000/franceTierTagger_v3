package nerd.amara;

import nerd.amara.tiers.PlayerInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RequestManager {
    private static final int THREAD_POOL_SIZE = 4;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
        Thread t = new Thread(r, "francetiers-fetch");
        t.setDaemon(true);
        return t;
    });

    private static final Set<String> pending = ConcurrentHashMap.newKeySet();

    public static void fetchPlayerInfo(String pseudo, Consumer<PlayerInfo> onSuccess, Consumer<Http.Status> onError) {
        PlayerInfo cached = PlayerInfoCache.get(pseudo);
        if (cached != null) {
            onSuccess.accept(cached);
            return;
        }

        if (!pending.add(pseudo)) {
            return;
        }

        executor.submit(() -> {
            try {
                Http.HttpResult<PlayerInfo> result = Http.getJson(Francetiers_tagger.web_url + pseudo, PlayerInfo.class);
                if (result.status == Http.Status.OK && result.data != null) {
                    PlayerInfoCache.put(pseudo, result.data);
                    onSuccess.accept(result.data);
                } else if (onError != null) {
                    onError.accept(result.status);
                }
            } finally {
                pending.remove(pseudo);
            }
        });
    }
}