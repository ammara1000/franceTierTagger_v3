package nerd.amara;

import nerd.amara.tiers.PlayerInfo;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PlayerInfoCache {
    private static final long TTL_FOUND_MILLIS = 5 * 60 * 1000L;
    private static final long TTL_NOT_FOUND_MILLIS = 5 * 60 * 1000L;
    private static final long TTL_ERROR_MILLIS = 30 * 1000L;
    private static final int MAX_ENTRIES = 300;

    public enum Result {
        FOUND,
        NOT_FOUND,
        ERROR
    }

    public static class CachedEntry {
        public final Result result;
        public final PlayerInfo info;
        public final Http.Status errorStatus;

        private CachedEntry(Result result, PlayerInfo info, Http.Status errorStatus) {
            this.result = result;
            this.info = info;
            this.errorStatus = errorStatus;
        }
    }

    private static class CacheRecord {
        final CachedEntry cachedEntry;
        final long timestamp;
        final long ttl;

        CacheRecord(CachedEntry cachedEntry, long timestamp, long ttl) {
            this.cachedEntry = cachedEntry;
            this.timestamp = timestamp;
            this.ttl = ttl;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }

    private static final Map<String, CacheRecord> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheRecord> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    private static String normalizeKey(String pseudo) {
        return pseudo == null ? "" : pseudo.toLowerCase(Locale.ROOT);
    }

    public static synchronized CachedEntry get(String pseudo) {
        CacheRecord record = cache.get(normalizeKey(pseudo));
        if (record == null) {
            return null;
        }
        return record.cachedEntry;
    }

    public static synchronized boolean isExpired(String pseudo) {
        CacheRecord record = cache.get(normalizeKey(pseudo));
        if (record == null) {
            return true;
        }
        return record.isExpired();
    }

    public static synchronized void putFound(String pseudo, PlayerInfo info) {
        cache.put(normalizeKey(pseudo), new CacheRecord(new CachedEntry(Result.FOUND, info, null), System.currentTimeMillis(), TTL_FOUND_MILLIS));
    }

    public static synchronized void putNotFound(String pseudo) {
        cache.put(normalizeKey(pseudo), new CacheRecord(new CachedEntry(Result.NOT_FOUND, null, Http.Status.NOT_FOUND), System.currentTimeMillis(), TTL_NOT_FOUND_MILLIS));
    }

    public static synchronized void putError(String pseudo, Http.Status status) {
        cache.put(normalizeKey(pseudo), new CacheRecord(new CachedEntry(Result.ERROR, null, status), System.currentTimeMillis(), TTL_ERROR_MILLIS));
    }

    public static synchronized void invalidate(String pseudo) {
        cache.remove(normalizeKey(pseudo));
    }

    public static synchronized void clear() {
        cache.clear();
    }
}
