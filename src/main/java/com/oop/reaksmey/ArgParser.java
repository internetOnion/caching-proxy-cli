package com.oop.reaksmey;

public class ArgParser {

	private final int port;
	private final String origin;
	private final boolean clearCache;

	private ArgParser(int port, String origin, boolean clearCache) {
		this.port = port;
		this.origin = origin;
		this.clearCache = clearCache;
	}

	public static ArgParser parse(String[] args) {
		int port = 3000;
		String origin = null;
		boolean clearCache = false;

		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			switch (arg) {
				case "--port":
					if (i + 1 >= args.length) {
						throw new IllegalArgumentException("--port requires a value");
					}
					try {
						port = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("--port must be a number");
					}
					if (port < 1 || port > 65535) {
						throw new IllegalArgumentException("--port must be between 1 and 65535");
					}
					i += 2;
					break;
				case "--origin":
					if (i + 1 >= args.length) {
						throw new IllegalArgumentException("--origin requires a value");
					}
					origin = args[i + 1];
					i += 2;
					break;
				case "--clear-cache":
					clearCache = true;
					i++;
					break;
				default:
					throw new IllegalArgumentException("unknown argument: " + arg);
			}
		}

		if (clearCache) {
			if (origin != null || port != 3000) {
				throw new IllegalArgumentException("--clear-cache cannot be used with --port or --origin");
			}
		} else {
			if (origin == null) {
				throw new IllegalArgumentException("--origin is required");
			}
		}

		return new ArgParser(port, origin, clearCache);
	}

	public int getPort() {
		return port;
	}

	public String getOrigin() {
		return origin;
	}

	public boolean isClearCache() {
		return clearCache;
	}
}
