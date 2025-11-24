package com.example.auth;

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

public class App {

    static RSAPrivateKey loadPrivateKey() throws Exception {
        String pem = new String(Files.readAllBytes(Paths.get("private.pem")));
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

        // ⭐ Auth service now runs on PORT 3000
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

        server.createContext("/login", new LoginHandler(algorithm));
        server.createContext("/.well-known/jwks.json", new JwksHandler());
        server.start();

        System.out.println("🚀 Auth Service started on port 3000");
    }

    static class LoginHandler implements HttpHandler {
        private final Algorithm algorithm;

        LoginHandler(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
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

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.length());
                exchange.getResponseBody().write(json.getBytes());
                exchange.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class JwksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                byte[] jwks = Files.readAllBytes(Paths.get("jwks.json"));
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jwks.length);
                exchange.getResponseBody().write(jwks);
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
