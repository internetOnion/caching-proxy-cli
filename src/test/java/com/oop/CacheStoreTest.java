package com.oop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CacheStoreTest {

	private CacheStore cache;

	@BeforeEach
	void setUp() {
		cache = new CacheStore();
	}

	@Test
	void putAndGet() {
		CachedResponse response = new CachedResponse(200, Map.of(), "body".getBytes());
		cache.put("/test", response);
		assertEquals(response, cache.get("/test"));
	}

	@Test
	void containsExistingKey() {
		cache.put("/test", new CachedResponse(200, Map.of(), "body".getBytes()));
		assertTrue(cache.contains("/test"));
	}

	@Test
	void doesNotContainMissingKey() {
		assertFalse(cache.contains("/missing"));
	}

	@Test
	void getMissingKeyReturnsNull() {
		assertNull(cache.get("/missing"));
	}

	@Test
	void overwriteExistingEntry() {
		CachedResponse first = new CachedResponse(200, Map.of(), "first".getBytes());
		CachedResponse second = new CachedResponse(404, Map.of(), "second".getBytes());
		cache.put("/test", first);
		cache.put("/test", second);
		assertEquals(second, cache.get("/test"));
		assertEquals(404, cache.get("/test").getStatusCode());
	}

	@Test
	void clearRemovesAllEntries() {
		cache.put("/a", new CachedResponse(200, Map.of(), "a".getBytes()));
		cache.put("/b", new CachedResponse(200, Map.of(), "b".getBytes()));
		cache.clear();
		assertFalse(cache.contains("/a"));
		assertFalse(cache.contains("/b"));
	}

	@Test
	void keysAreExactMatch() {
		cache.put("/users?id=1", new CachedResponse(200, Map.of(), "body".getBytes()));
		assertTrue(cache.contains("/users?id=1"));
		assertFalse(cache.contains("/users?id=2"));
	}
}
