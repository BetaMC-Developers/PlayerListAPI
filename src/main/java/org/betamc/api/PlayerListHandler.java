package org.betamc.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.zavdav.zcore.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public class PlayerListHandler implements HttpHandler {

    private final Gson gson;
    private final List<String> apiKeys;

    PlayerListHandler(List<String> apiKeys) {
        this.gson = new GsonBuilder().serializeNulls().create();
        this.apiKeys = apiKeys;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JsonObject response = new JsonObject();
        String apiKey = getApiKey(exchange);
        boolean hasApiKey = apiKey != null && apiKeys.contains(apiKey);

        response.addProperty("player_count", Bukkit.getOnlinePlayers().length);
        response.addProperty("max_players", Bukkit.getMaxPlayers());

        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            JsonObject jsonData = new JsonObject();
            addPublicPlayerData(jsonData, player);
            if (hasApiKey) addPrivatePlayerData(jsonData, player);
            response.add(String.valueOf(i++), jsonData);
        }

        sendResponse(exchange, gson.toJson(response));
    }

    private String getApiKey(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals("apiKey") && !pair[1].isEmpty()) {
                return pair[1];
            }
        }

        return null;
    }

    private void addPublicPlayerData(JsonObject jsonData, Player player) {
        jsonData.addProperty("uuid", player.getUniqueId().toString());
        jsonData.addProperty("username", player.getName());
        jsonData.addProperty("display_name", player.getDisplayName());

        User user = User.Companion.from(player);
        jsonData.addProperty("nickname", user.getNickname());
        jsonData.addProperty("first_join", user.getFirstJoin());
        jsonData.addProperty("last_join", user.getLastJoin());
        jsonData.addProperty("last_seen", user.getLastSeen());
        jsonData.addProperty("balance", user.getBalance());
        user.updatePlayTime();
        jsonData.addProperty("playtime", user.getPlayTime());
    }

    private void addPrivatePlayerData(JsonObject jsonData, Player player) {
        Location location = player.getLocation();
        jsonData.addProperty("world", location.getWorld().getName());
        jsonData.addProperty("x", location.getX());
        jsonData.addProperty("y", location.getY());
        jsonData.addProperty("z", location.getZ());

        User user = User.Companion.from(player);
        jsonData.addProperty("god", user.isGod());
        jsonData.addProperty("vanish", user.isVanished());
        jsonData.addProperty("socialspy", user.getSocialSpy());
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes();

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);

        exchange.close();
    }

}
