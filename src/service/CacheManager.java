package service;

// import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Gestionnaire de cache pour optimiser les requêtes répétées et réduire la charge sur la base de données.
 * Cette classe fournit un mécanisme centralisé de cache pour les données métier.
 * 
 * @author Sado Adonya & VIEYRA Kolawole
 * @version 1.0
 */
public class CacheManager {
	/**
	 * Internal cache entry holding a value and its expiration timestamp.
	 */
	private static class Entry {
		final Object value;
		final long expiresAt;

		/**
		 * Creates a cache entry with a value and expiration timestamp.
		 *
		 * @param value     the value to cache
		 * @param expiresAt the expiration timestamp in milliseconds, or 0 for no expiration
		 */
		Entry(Object value, long expiresAt) {
			this.value = value;
			this.expiresAt = expiresAt;
		}

		/**
		 * Checks if this cache entry has expired.
		 *
		 * @param now the current timestamp in milliseconds
		 * @return true if the entry has expired, false otherwise
		 */
		boolean isExpired(long now) {
			return expiresAt > 0 && now >= expiresAt;
		}
	}

	private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
	private static final long DEFAULT_TTL_MS = 15_000L;

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private CacheManager() {
	}

	/**
	 * Retrieves a value from cache or loads it using the supplier with the default TTL (15 seconds).
	 *
	 * @param <T>    the type of the value
	 * @param key    the cache key
	 * @param loader the supplier to load the value if not cached
	 * @return the cached or freshly loaded value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrLoad(String key, Supplier<T> loader) {
		return getOrLoad(key, DEFAULT_TTL_MS, loader);
	}

	/**
	 * Retrieves a value from cache or loads it using the supplier with a specified TTL.
	 *
	 * @param <T>       the type of the value
	 * @param key       the cache key
	 * @param ttlMillis the time-to-live in milliseconds, or 0 for no expiration
	 * @param loader    the supplier to load the value if not cached
	 * @return the cached or freshly loaded value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrLoad(String key, long ttlMillis, Supplier<T> loader) {
		if (key == null || key.isBlank() || loader == null) {
			return loader == null ? null : loader.get();
		}
		long now = System.currentTimeMillis();
		Entry cached = CACHE.get(key);
		if (cached != null && !cached.isExpired(now)) {
			return (T) cached.value;
		}
		T value = loader.get();
		CACHE.put(key, new Entry(value, ttlMillis <= 0 ? 0 : now + ttlMillis));
		return value;
	}

	/**
	 * Stores a value in cache with no expiration.
	 *
	 * @param key   the cache key
	 * @param value the value to cache
	 */
	public static void put(String key, Object value) {
		if (key == null || key.isBlank()) {
			return;
		}
		CACHE.put(key, new Entry(value, 0));
	}

	/**
	 * Removes a single entry from cache by key.
	 *
	 * @param key the cache key to remove
	 */
	public static void invalidate(String key) {
		if (key == null || key.isBlank()) {
			return;
		}
		CACHE.remove(key);
	}

	/**
	 * Removes all cache entries whose key starts with the given prefix.
	 *
	 * @param prefix the key prefix to match
	 */
	public static void invalidatePrefix(String prefix) {
		if (prefix == null || prefix.isBlank()) {
			return;
		}
        CACHE.keySet().removeIf(key -> key.startsWith(prefix));
	}
}
