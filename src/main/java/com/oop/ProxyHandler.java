package com.oop;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyHandler implements HttpHandler {

	private final String origin;
	private final CacheStore cache;
	private final HttpClient client;

	public ProxyHandler(String origin, CacheStore cache) {
		this.origin = origin.replaceAll("/$", "");
		this.cache = cache;
		this.client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.connectTimeout(Duration.ofSeconds(30))
				.build();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String method = exchange.getRequestMethod();
		boolean isGet = "GET".equalsIgnoreCase(method);
		String cacheKey = exchange.getRequestURI().toString();

		if (isGet && cache.contains(cacheKey)) {
			serveFromCache(exchange, cacheKey);
			return;
		}

		proxyToOrigin(exchange, cacheKey);
	}

	private void serveFromCache(HttpExchange exchange, String cacheKey) throws IOException {
		CachedResponse cached = cache.get(cacheKey);

		for (Map.Entry<String, List<String>> headerEntry : cached.getHeaders().entrySet()) {
			for (String value : headerEntry.getValue()) {
				exchange.getResponseHeaders().add(headerEntry.getKey(), value);
			}
		}
		exchange.getResponseHeaders().set("X-Cache", "HIT");
		exchange.getResponseHeaders().set("Via", "caching-proxy/1.0");

		byte[] body = cached.getBody();
		exchange.sendResponseHeaders(cached.getStatusCode(), body.length);
		if (body.length > 0) {
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
		}

		log("GET", cacheKey, cached.getStatusCode(), "HIT");
	}

	private void proxyToOrigin(HttpExchange exchange, String cacheKey) throws IOException {
		String method = exchange.getRequestMethod();
		String targetUrl = origin + exchange.getRequestURI().toString();

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(targetUrl))
				.timeout(Duration.ofSeconds(30));

		copyRequestHeaders(exchange, requestBuilder);

		byte[] requestBody = exchange.getRequestBody().readAllBytes();
		HttpRequest.BodyPublisher publisher = requestBody.length > 0
				? HttpRequest.BodyPublishers.ofByteArray(requestBody)
				: HttpRequest.BodyPublishers.noBody();
		requestBuilder.method(method.toUpperCase(), publisher);

		HttpResponse<byte[]> response;
		try {
			response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			sendError(exchange, "502 Bad Gateway: interrupted");
			log(method, cacheKey, 502, "MISS");
			return;
		} catch (IOException e) {
			sendError(exchange, "502 Bad Gateway: " + e.getMessage());
			log(method, cacheKey, 502, "MISS");
			return;
		}

		Map<String, List<String>> responseHeaders = new HashMap<>(response.headers().map());
		responseHeaders.remove("x-cache");

		int statusCode = response.statusCode();
		byte[] body = response.body();

		if ("GET".equalsIgnoreCase(method)) {
			cache.put(cacheKey, new CachedResponse(statusCode, responseHeaders, body));
		}

		for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
			for (String value : headerEntry.getValue()) {
				exchange.getResponseHeaders().add(headerEntry.getKey(), value);
			}
		}
		exchange.getResponseHeaders().set("X-Cache", "MISS");
		exchange.getResponseHeaders().set("Via", "caching-proxy/1.0");

		exchange.sendResponseHeaders(statusCode, body.length);
		if (body.length > 0) {
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
		}

		log(method, cacheKey, statusCode, "MISS");
	}

	private void copyRequestHeaders(HttpExchange exchange, HttpRequest.Builder builder) {
		for (Map.Entry<String, List<String>> headerEntry : exchange.getRequestHeaders().entrySet()) {
			String name = headerEntry.getKey();
			if (name == null) {
				continue;
			}
			String lowerName = name.toLowerCase();
			if ("host".equals(lowerName) || "connection".equals(lowerName) || "transfer-encoding".equals(lowerName)) {
				continue;
			}
			for (String value : headerEntry.getValue()) {
				builder.header(name, value);
			}
		}
		URI originUri = URI.create(origin);
		builder.header("Host", originUri.getHost());
	}

	private void sendError(HttpExchange exchange, String message) throws IOException {
		byte[] errorBytes = message.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "text/plain");
		exchange.getResponseHeaders().set("X-Cache", "MISS");
		exchange.sendResponseHeaders(502, errorBytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(errorBytes);
		}
	}

	private void log(String method, String path, int statusCode, String cacheStatus) {
		System.out.printf("%-6s %-40s %3d %s%n", method, path, statusCode, cacheStatus);
	}
}
