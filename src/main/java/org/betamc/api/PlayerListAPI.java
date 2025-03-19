package org.betamc.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class PlayerListAPI extends JavaPlugin {

    private HttpServer server;

    @Override
    public void onEnable() {
        getConfiguration().load();
        String route = getConfiguration().getString("route", "/api/players");
        String protocol = getConfiguration().getString("protocol", "http").toUpperCase();
        int port = getConfiguration().getInt("port", 8080);
        List<String> apiKeys = getConfiguration().getStringList("apiKeys", new ArrayList<String>(){{ add("changethis"); }});
        getConfiguration().setProperty("apiKeys", apiKeys);
        getConfiguration().save();

        try {
            if (protocol.equals("HTTPS")) {
                server = HttpsServer.create(new InetSocketAddress(port), 0);
                setupHttpsServer((HttpsServer) server);
            } else {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        server.createContext(route, new PlayerListHandler(apiKeys));
        server.start();
        Bukkit.getLogger().info("[" + getDescription().getName() + "] Server is listening on port " + port);
    }

    private void setupHttpsServer(HttpsServer server) throws Exception {
        getConfiguration().load();
        char[] password = getConfiguration().getString("keystore_password", "changethis").toCharArray();
        getConfiguration().save();

        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(new File(getDataFolder(), "keystore.jks"));
        ks.load(fis, password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        server.setHttpsConfigurator(new HttpsConfigurator(context));
    }

    @Override
    public void onDisable() {
        server.stop(0);
        Bukkit.getLogger().info("[" + getDescription().getName() + "] Server has been stopped");
    }
}
