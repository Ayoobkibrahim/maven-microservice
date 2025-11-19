package com.example.profile;

import com.sun.net.httpserver.*;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {
        int port = 8082;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/profiles", new ProfileHandler());
        server.start();
        System.out.println("Profile Service started on port " + port);
    }

    static class ProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {

                String json = """
                {
                  "id": 1,
                  "bio": "Software Engineer",
                  "skills": ["Java","Kubernetes","Docker"]
                }
                """;

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);

                try(OutputStream os = exchange.getResponseBody()) {
                    os.write(json.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
