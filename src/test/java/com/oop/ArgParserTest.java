package com.oop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgParserTest {

	@Test
	void serverModeWithPortAndOrigin() {
		ArgParser args = ArgParser.parse(new String[]{"--port", "8080", "--origin", "https://example.com"});
		assertEquals(8080, args.getPort());
		assertEquals("https://example.com", args.getOrigin());
		assertFalse(args.isClearCache());
	}

	@Test
	void serverModeDefaultPort() {
		ArgParser args = ArgParser.parse(new String[]{"--origin", "https://example.com"});
		assertEquals(3000, args.getPort());
		assertEquals("https://example.com", args.getOrigin());
		assertFalse(args.isClearCache());
	}

	@Test
	void clearCacheMode() {
		ArgParser args = ArgParser.parse(new String[]{"--clear-cache"});
		assertTrue(args.isClearCache());
		assertNull(args.getOrigin());
		assertEquals(3000, args.getPort());
	}

	@Test
	void clearCacheCannotBeCombinedWithOrigin() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--clear-cache", "--origin", "https://example.com"}));
	}

	@Test
	void clearCacheCannotBeCombinedWithPort() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--clear-cache", "--port", "8080"}));
	}

	@Test
	void originIsRequiredForServerMode() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--port", "8080"}));
	}

	@Test
	void portMustBeNumber() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--port", "abc", "--origin", "https://example.com"}));
	}

	@Test
	void portMustBeInRange() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--port", "99999", "--origin", "https://example.com"}));
	}

	@Test
	void portRequiresValue() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--port"}));
	}

	@Test
	void originRequiresValue() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--origin"}));
	}

	@Test
	void unknownArgumentThrows() {
		assertThrows(IllegalArgumentException.class, () ->
				ArgParser.parse(new String[]{"--verbose"}));
	}
}
