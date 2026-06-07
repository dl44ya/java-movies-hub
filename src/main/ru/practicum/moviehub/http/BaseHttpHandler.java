package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected void sendJson(HttpExchange ex, int status, String response) throws IOException {
        try (OutputStream os = ex.getResponseBody()) {
            ex.getResponseHeaders().set("Content-Type", CT_JSON);
            ex.sendResponseHeaders(status, 0);
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        ex.close();
    }

    protected void sendErrorJson(HttpExchange ex, int status, String response) throws IOException {

        try (OutputStream os = ex.getResponseBody()) {
            ex.getResponseHeaders().set("Content-Type", CT_JSON);
            ex.sendResponseHeaders(status, 0);
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        ex.close();
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        try (OutputStream os = ex.getResponseBody()) {
            ex.getResponseHeaders().set("Content-Type", CT_JSON);
            ex.sendResponseHeaders(204, -1);
        }
        ex.close();
    }
}