package org.betamc.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

public class PlayerListHandler implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JsonObject response = new JsonObject();

        response.addProperty("player_count", Bukkit.getOnlinePlayers().length);
        response.addProperty("max_players", Bukkit.getMaxPlayers());

        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            JsonObject playerJson = new JsonObject();
            playerJson.addProperty("uuid", player.getUniqueId().toString());
            playerJson.addProperty("username", player.getName());
            playerJson.addProperty("display_name", player.getDisplayName());

            response.add(String.valueOf(i++), playerJson);
        }

        sendResponse(exchange, gson.toJson(response));
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes();

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);

        exchange.close();
    }

}
