package nerd.amara;

import nerd.amara.tiers.PlayerInfo;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerInfoCache {
    private static final long TTL_MILLIS = 5 * 60 * 1000; // 5 min
    private static final int MAX_ENTRIES = 150;

    private static class Entry {
        final PlayerInfo info;
        final long timestamp;
        Entry(PlayerInfo info, long timestamp) {
            this.info = info;
            this.timestamp = timestamp;
        }
    }

    private static final Map<String, Entry> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public static synchronized PlayerInfo get(String pseudo) {
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

    public static synchronized void put(String pseudo, PlayerInfo info) {
        cache.put(pseudo, new Entry(info, System.currentTimeMillis()));
    }
}