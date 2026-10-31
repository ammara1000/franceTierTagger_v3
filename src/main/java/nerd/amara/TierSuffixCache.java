package nerd.amara;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TierSuffixCache {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    private static String normalize(String pseudo) {
        return pseudo == null ? "" : pseudo.toLowerCase(Locale.ROOT);
    }

    public static String getSuffix(String pseudo) {
        if (pseudo == null || pseudo.isEmpty()) {
            return "";
        }
        String key = normalize(pseudo);
        return cache.computeIfAbsent(key, k -> {
            PlayerInfoCache.CachedEntry entry = PlayerInfoCache.get(k);
            if (entry != null && entry.result == PlayerInfoCache.Result.FOUND && entry.info != null) {
                return ShowedTier.showed_tier(entry.info);
            }
            return "";
        });
    }

    public static void putSuffix(String pseudo, String suffix) {
        if (pseudo != null) {
            cache.put(normalize(pseudo), suffix != null ? suffix : "");
        }
    }

    public static void invalidate(String pseudo) {
        if (pseudo != null) {
            cache.remove(normalize(pseudo));
        }
    }

    public static void clear() {
        cache.clear();
    }
}
