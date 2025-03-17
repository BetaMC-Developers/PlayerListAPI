package org.betamc.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.zavdav.zcore.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

public class PlayerListHandler implements HttpHandler {

    private final Gson gson = new GsonBuilder().serializeNulls().create();

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

            User user = User.Companion.from(player);

            playerJson.addProperty("nickname", user.getNickname());
            playerJson.addProperty("first_join", user.getFirstJoin());
            playerJson.addProperty("last_join", user.getLastJoin());
            playerJson.addProperty("last_seen", user.getLastSeen());
            playerJson.addProperty("balance", user.getBalance());
            user.updatePlayTime();
            playerJson.addProperty("playtime", user.getPlayTime());

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
