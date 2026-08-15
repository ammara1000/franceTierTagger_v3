package nerd.amara;

import nerd.amara.tiers.PlayerInfo;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerInfoCache {
    private static final long TTL_MILLIS = 5 * 60 * 1000;
    private static final int MAX_ENTRIES = 150;

    public enum Result {
        FOUND,
        NOT_FOUND
    }

    public static class CachedEntry {
        public final Result result;
        public final PlayerInfo info;
        private CachedEntry(Result result, PlayerInfo info) {
            this.result = result;
            this.info = info;
        }
    }

    private static class Entry {
        final CachedEntry cachedEntry;
        final long timestamp;
        Entry(CachedEntry cachedEntry, long timestamp) {
            this.cachedEntry = cachedEntry;
            this.timestamp = timestamp;
        }
    }

    private static final Map<String, Entry> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public static synchronized CachedEntry get(String pseudo) {
        Entry entry = cache.get(pseudo);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.timestamp > TTL_MILLIS) {
            cache.remove(pseudo);
            return null;
        }
        return entry.cachedEntry;
    }

    public static synchronized void putFound(String pseudo, PlayerInfo info) {
        cache.put(pseudo, new Entry(new CachedEntry(Result.FOUND, info), System.currentTimeMillis()));
    }

    public static synchronized void putNotFound(String pseudo) {
        cache.put(pseudo, new Entry(new CachedEntry(Result.NOT_FOUND, null), System.currentTimeMillis()));
    }

    public static synchronized void invalidate(String pseudo) {
        cache.remove(pseudo);
    }
}