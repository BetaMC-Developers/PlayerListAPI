package org.betamc.api;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PlayerListAPI extends JavaPlugin {

    private HttpServer server;

    @Override
    public void onEnable() {
        Configuration config = getConfiguration();
        config.load();
        int port = config.getInt("port", 8080);

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        config.save();
        server.createContext("/playerlist", new PlayerListHandler());
        server.start();
        Bukkit.getLogger().info("[" + getDescription().getName() + "] Server is listening on port " + port);
    }

    @Override
    public void onDisable() {
        server.stop(0);
        Bukkit.getLogger().info("[" + getDescription().getName() + "] Server has been stopped");
    }
}
