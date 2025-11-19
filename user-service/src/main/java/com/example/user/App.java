package com.example.user;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.io.OutputStream;

public class App {

    public static void main(String[] args) throws Exception {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/users", new UserHandler());
        server.start();
        System.out.println("User Service started on port " + port);
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {

                String json = """
                [
                  {"id":1,"name":"Ayoob","email":"ayoob@example.com"},
                  {"id":2,"name":"Fayiz","email":"fayiz@example.com"},
                  {"id":3,"name":"Havva","email":"havva@example.com"},
                  {"id":4,"name":"Ashiq","email":"ashiq@example.com"},
                  {"id":5,"name":"Shana","email":"shana@example.com"},
                  {"id":6,"name":"Arjun","email":"arjun@example.com"},
                  {"id":7,"name":"Rabeeh","email":"rabeeh@example.com"},
                  {"id":8,"name":"Sinan","email":"sinan@example.com"},
                  {"id":9,"name":"Baheeja","email":"baheeja@example.com"},
                  {"id":10,"name":"Amal","email":"amal@example.com"}
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
