package com.example.auth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.nio.charset.StandardCharsets;

public class App {

    static RSAPrivateKey loadPrivateKey() throws Exception {
        String pem = new String(Files.readAllBytes(Paths.get("private.pem")), StandardCharsets.UTF_8);
        pem = pem.replaceAll("-----BEGIN (.*)-----", "")
                 .replaceAll("-----END (.*)-----", "")
                 .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    public static void main(String[] args) throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey();
        Algorithm algorithm = Algorithm.RSA256(null, privateKey);

        // Auth service now runs on PORT 3000
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

        server.createContext("/login", new LoginHandler(algorithm));
        server.createContext("/.well-known/jwks.json", new JwksHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("🚀 Auth Service started on port 3000");
    }

    // Helper to add CORS headers
    static void addCorsHeaders(HttpExchange exchange) {
        Headers resp = exchange.getResponseHeaders();
        // Allow any origin for local/dev. Change to exact origin for production e.g. "https://your-app.example"
        resp.set("Access-Control-Allow-Origin", "*");
        resp.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.set("Access-Control-Max-Age", "3600");
        // If you want to allow credentials (cookies/authorization) set to "true" and configure origin specifically
        // resp.set("Access-Control-Allow-Credentials", "true");
    }

    static boolean handlePreflight(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            addCorsHeaders(exchange);
            // no body for preflight
            exchange.sendResponseHeaders(204, -1); // no response body
            exchange.close();
            return true;
        }
        return false;
    }

    static class LoginHandler implements HttpHandler {
        private final Algorithm algorithm;

        LoginHandler(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
                // respond to preflight immediately
                if (handlePreflight(exchange)) return;

                long now = System.currentTimeMillis();

                String token = JWT.create()
                        .withIssuer("auth.example.com")
                        .withSubject("user123")
                        .withClaim("role", "student")
                        .withIssuedAt(new Date(now))
                        .withExpiresAt(new Date(now + 3600_000))
                        .withKeyId("auth-rs256-key-1")
                        .sign(algorithm);

                String json = "{ \"token\": \"" + token + "\" }";

                addCorsHeaders(exchange);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                byte[] respBytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, respBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(respBytes);
                }
                exchange.close();

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    addCorsHeaders(exchange);
                    String err = "{\"error\":\"internal_server_error\"}";
                    byte[] errBytes = err.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, errBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errBytes);
                    }
                    exchange.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static class JwksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                // handle preflight
                if (handlePreflight(exchange)) return;

                byte[] jwks = Files.readAllBytes(Paths.get("jwks.json"));
                addCorsHeaders(exchange);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jwks.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jwks);
                }
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    addCorsHeaders(exchange);
                    String err = "{\"error\":\"internal_server_error\"}";
                    byte[] errBytes = err.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(500, errBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errBytes);
                    }
                    exchange.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
