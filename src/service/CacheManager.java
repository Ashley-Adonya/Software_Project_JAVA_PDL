package service;

import java.util.Iterator;
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
	private static class Entry {
		final Object value;
		final long expiresAt;

		Entry(Object value, long expiresAt) {
			this.value = value;
			this.expiresAt = expiresAt;
		}

		boolean isExpired(long now) {
			return expiresAt > 0 && now >= expiresAt;
		}
	}

	private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
	private static final long DEFAULT_TTL_MS = 15_000L;

	private CacheManager() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T getOrLoad(String key, Supplier<T> loader) {
		return getOrLoad(key, DEFAULT_TTL_MS, loader);
	}

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

	public static void put(String key, Object value) {
		if (key == null || key.isBlank()) {
			return;
		}
		CACHE.put(key, new Entry(value, 0));
	}

	public static void invalidate(String key) {
		if (key == null || key.isBlank()) {
			return;
		}
		CACHE.remove(key);
	}

	public static void invalidatePrefix(String prefix) {
		if (prefix == null || prefix.isBlank()) {
			return;
		}
        CACHE.keySet().removeIf(key -> key.startsWith(prefix));
	}
}
