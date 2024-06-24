package fr.islandswars.ineundo;

import com.google.inject.Inject;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClients;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.islandswars.commons.service.mongodb.MongoDBConnection;
import fr.islandswars.commons.utils.LogUtils;
import fr.islandswars.ineundo.listener.PlayerDataListener;
import fr.islandswars.ineundo.player.IslandsPlayer;
import net.kyori.adventure.text.Component;
import org.bson.UuidRepresentation;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * File <b>Ineundo</b> located on fr.islandswars.ineundo
 * Ineundo is a part of ineundo.
 * <p>
 * Copyright (c) 2017 - 2024 Islands Wars.
 * <p>
 * ineundo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <a href="http://www.gnu.org/licenses/">GNU license</a>.
 * <p>
 *
 * @author Jangliu, {@literal <jangliu@islandswars.fr>}
 * Created the 18/06/2024 at 00:10
 * @since 0.1
 */
@Plugin(
        id = "ineundo",
        name = "Ineundo",
        version = "0.1",
        authors = "Xharos"
)
public class Ineundo {

    private final CopyOnWriteArrayList<IslandsPlayer> players;
    private final MongoDBConnection                   mongoConnection;
    private final Logger                              logger;
    private final ProxyServer                         server;

    @Inject
    public Ineundo(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        this.mongoConnection = new MongoDBConnection();
        this.players = new CopyOnWriteArrayList<>();
        this.logger = logger;
        this.server = server;
        LogUtils.setErrorConsummer(e -> {
            e.printStackTrace();//TODO change
        });
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        try {
            mongoConnection.load();
            mongoConnection.connect();
            logger.info(mongoConnection.getConnection().getName());
        } catch (Exception e) {
            server.shutdown(Component.text("Database issue"));
            e.printStackTrace();
        }

        new PlayerDataListener(this, mongoConnection);
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public void addPlayer(IslandsPlayer player) {
        if (getPlayer(player.getUUID()).isPresent())
            logger.severe("Player " + player.getUUID() + " is already registered....");
        else
            players.add(player);
    }

    public Optional<IslandsPlayer> getPlayer(UUID uuid) {
        return players.stream().filter(p -> p.getUUID().equals(uuid)).findFirst();
    }

    public CopyOnWriteArrayList<IslandsPlayer> getPlayers() {
        return players;
    }

    public void removePlayer(IslandsPlayer player) {
        players.remove(player);
    }
}
