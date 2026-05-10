package com.oop;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedResponseTest {

	@Test
	void storesStatusCode() {
		CachedResponse response = new CachedResponse(200, Map.of(), new byte[0]);
		assertEquals(200, response.getStatusCode());
	}

	@Test
	void storesHeaders() {
		Map<String, List<String>> headers = Map.of("content-type", List.of("text/html"));
		CachedResponse response = new CachedResponse(200, headers, new byte[0]);
		assertEquals(headers, response.getHeaders());
	}

	@Test
	void storesBody() {
		byte[] body = "hello".getBytes();
		CachedResponse response = new CachedResponse(200, Map.of(), body);
		assertArrayEquals(body, response.getBody());
	}
}
