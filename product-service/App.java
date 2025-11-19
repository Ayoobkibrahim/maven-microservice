package com.example.product;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.io.OutputStream;

public class App {

    public static void main(String[] args) throws Exception {
        int port = 8084;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/products", new ProductHandler());
        server.start();
        System.out.println("Product Service started on port " + port);
    }

    static class ProductHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                String json = """
                [
                  {"productId":1, "name":"Laptop", "price":55000},
                  {"productId":2, "name":"Mobile", "price":30000},
                  {"productId":3, "name":"Headphones", "price":1500},
                  {"productId":4, "name":"Keyboard", "price":1200},
                  {"productId":5, "name":"Mouse", "price":800}
                ]
                """;

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(json.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
