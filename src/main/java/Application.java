package com.oop;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Application {

	public static void main(String[] args) throws IOException {
		ArgParser parsed;
		try {
			parsed = ArgParser.parse(args);
		} catch (IllegalArgumentException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
			return;
		}

		if (parsed.isClearCache()) {
			System.out.println("Cache cleared.");
			return;
		}

		validateOrigin(parsed.getOrigin());

		CacheStore cache = new CacheStore();
		HttpServer server = HttpServer.create(new InetSocketAddress(parsed.getPort()), 0);
		server.createContext("/", new ProxyHandler(parsed.getOrigin(), cache));
		server.setExecutor(null);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down caching proxy...");
			server.stop(2);
		}));

		server.start();
		System.out.println("Caching proxy server started on port " + parsed.getPort()
				+ ", forwarding to " + parsed.getOrigin());
	}

	private static void validateOrigin(String origin) {
		URI uri;
		try {
			uri = URI.create(origin);
		} catch (IllegalArgumentException e) {
			System.err.println("Error: invalid origin URL: " + origin);
			System.exit(1);
			return;
		}

		String scheme = uri.getScheme();
		if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
			System.err.println("Error: origin must start with http:// or https://");
			System.exit(1);
		}

		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();
			HttpRequest probe = HttpRequest.newBuilder()
					.uri(uri)
					.method("HEAD", HttpRequest.BodyPublishers.noBody())
					.timeout(Duration.ofSeconds(5))
					.build();
			client.send(probe, HttpResponse.BodyHandlers.discarding());
		} catch (Exception e) {
			System.out.println("Warning: could not reach origin at " + origin + " (" + e.getMessage() + ")");
		}
	}
}
