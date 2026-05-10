package com.oop;

import java.util.concurrent.ConcurrentHashMap;

public class CacheStore {

	private final ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();

	public CachedResponse get(String key) {
		return cache.get(key);
	}

	public void put(String key, CachedResponse response) {
		cache.put(key, response);
	}

	public boolean contains(String key) {
		return cache.containsKey(key);
	}

	public void clear() {
		cache.clear();
	}
}
