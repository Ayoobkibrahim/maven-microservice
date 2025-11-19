package com.example.order;

import com.sun.net.httpserver.*;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {
        int port = 8083;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/orders", new OrderHandler());
        server.start();
        System.out.println("Order Service started on port " + port);
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {

                String json = """
                [
                  {"orderId":101,"userId":1,"amount":299,"status":"Delivered"},
                  {"orderId":102,"userId":1,"amount":499,"status":"Shipped"}
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
