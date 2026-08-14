package nerd.amara;

import nerd.amara.tiers.PlayerInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInfoCache {
    private static final long TTL_MILLIS = 5 * 60 * 1000; // 5 min
    private static final Map<String, Entry> cache = new ConcurrentHashMap<>();

    private static class Entry {
        final PlayerInfo info;
        final long timestamp;
        Entry(PlayerInfo info, long timestamp) {
            this.info = info;
            this.timestamp = timestamp;
        }
    }

    public static PlayerInfo get(String pseudo) {
        Entry entry = cache.get(pseudo);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.timestamp > TTL_MILLIS) {
            cache.remove(pseudo);
            return null;
        }
        return entry.info;
    }

    public static void put(String pseudo, PlayerInfo info) {
        cache.put(pseudo, new Entry(info, System.currentTimeMillis()));
    }
}