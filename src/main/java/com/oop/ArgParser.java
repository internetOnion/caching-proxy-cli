package com.oop;

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
						System.err.println("Error: --port requires a value");
						System.exit(1);
					}
					try {
						port = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException e) {
						System.err.println("Error: --port must be a number");
						System.exit(1);
					}
					if (port < 1 || port > 65535) {
						System.err.println("Error: --port must be between 1 and 65535");
						System.exit(1);
					}
					i += 2;
					break;
				case "--origin":
					if (i + 1 >= args.length) {
						System.err.println("Error: --origin requires a value");
						System.exit(1);
					}
					origin = args[i + 1];
					i += 2;
					break;
				case "--clear-cache":
					clearCache = true;
					i++;
					break;
				default:
					System.err.println("Error: unknown argument: " + arg);
					System.exit(1);
			}
		}

		if (clearCache) {
			if (origin != null || port != 3000) {
				System.err.println("Error: --clear-cache cannot be used with --port or --origin");
				System.exit(1);
			}
		} else {
			if (origin == null) {
				System.err.println("Error: --origin is required");
				System.exit(1);
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
