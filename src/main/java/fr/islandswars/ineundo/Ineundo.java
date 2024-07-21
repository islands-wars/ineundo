package fr.islandswars.ineundo;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.service.docker.DockerConnection;
import fr.islandswars.commons.service.mongodb.MongoDBConnection;
import fr.islandswars.commons.service.rabbitmq.RabbitMQConnection;
import fr.islandswars.commons.service.rabbitmq.packet.Packet;
import fr.islandswars.commons.service.redis.RedisConnection;
import fr.islandswars.ineundo.listener.PlayerDataListener;
import fr.islandswars.ineundo.listener.ServerPingListener;
import fr.islandswars.ineundo.locale.TranslationLoader;
import fr.islandswars.ineundo.log.InternalLogger;
import fr.islandswars.ineundo.manager.IneundoManager;
import fr.islandswars.ineundo.player.ProxyPlayer;
import fr.islandswars.ineundo.utils.ProxyConstants;
import fr.islandswars.ineundo.utils.RabbitConstants;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

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
 * TODO proxy shutdown save player and disconnect server, clean redis
 */
@Plugin(
        id = "ineundo",
        name = "Ineundo",
        version = "0.1",
        authors = "Xharos"
)
public class Ineundo {

    private static Ineundo                           INSTANCE;
    private final  CopyOnWriteArrayList<ProxyPlayer> players;
    private final  MongoDBConnection                 mongoConnection;
    private final  RedisConnection                   redisConnection;
    private final  RabbitMQConnection                rabbitMQConnection;
    private final  DockerConnection                  dockerConnection;
    private final  ProxyServer                       server;
    private final  AtomicBoolean                     STAFF_ONLY;
    private final  String                            velocitySecret;
    private        IneundoManager                    manager;
    private final  UUID                              proxyId;
    private        String                            containerName;

    @Inject
    public Ineundo(ProxyServer server) {
        if (INSTANCE == null)
            INSTANCE = this;
        this.proxyId = UUID.randomUUID();
        this.mongoConnection = new MongoDBConnection();
        this.redisConnection = new RedisConnection();
        this.rabbitMQConnection = new RabbitMQConnection();
        this.dockerConnection = new DockerConnection();
        this.players = new CopyOnWriteArrayList<>();
        this.STAFF_ONLY = new AtomicBoolean(false);
        this.server = server;
        this.velocitySecret = System.getenv(ProxyConstants.PROXY_SECRET_KEY);
        new InternalLogger(getContainerName());
    }

    public static Ineundo getInstance() {
        return INSTANCE;
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        if (this.velocitySecret == null)
            server.shutdown(Component.translatable("proxy.startup.secret"));
        new TranslationLoader().load("locale.ineundo");
        //databases
        try {
            mongoConnection.load();
            redisConnection.load();
            rabbitMQConnection.load();
            dockerConnection.load();
            mongoConnection.connect();
            redisConnection.connect();
            rabbitMQConnection.connect();
            dockerConnection.connect();
        } catch (Exception e) {
            IslandsLogger.getLogger().logError(e);
            server.shutdown(Component.translatable("proxy.startup.database.error"));
        }

        //listeners
        IslandsLogger.getLogger().logInfo("Starting new proxy...");
        new PlayerDataListener(this, mongoConnection, redisConnection.getConnection());
        new ServerPingListener(this, redisConnection.getConnection());
        this.manager = new IneundoManager(this, redisConnection.getConnection(), rabbitMQConnection, dockerConnection, velocitySecret);
        manager.initialize();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onQuit(ProxyShutdownEvent event) {
        IslandsLogger.getLogger().logInfo("Stoping proxy...");
        try {
            manager.shutdown();

            //TODO thread.sleep ?

            mongoConnection.close();
            redisConnection.close();
            rabbitMQConnection.close();
            dockerConnection.close();
        } catch (Exception e) {
            IslandsLogger.getLogger().logError(e);
        }
    }

    public void sendPacketToProxies(Packet packet) {
        sendPacket(packet, RabbitConstants.getProxiesQueue());
    }

    public void sendPacketToProxy(Packet packet, UUID proxyId) {
        sendPacket(packet, RabbitConstants.getProxyQueue(proxyId));
    }

    public void sendPacket(Packet packet, String routingKey) {
        manager.sendPacket(packet, routingKey);
    }

    public UUID getProxyId() {
        return proxyId;
    }

    public String getContainerName() {
        if (containerName == null)
            this.containerName = "proxy_" + getProxyId().toString();
        return this.containerName;
    }

    public ProxyServer getServer() {
        return server;
    }

    public AtomicBoolean getSTAFF_ONLY() {
        return STAFF_ONLY;
    }

    public void addPlayer(ProxyPlayer player) {
        if (getPlayer(player.getUUID()).isPresent())
            IslandsLogger.getLogger().log(Level.WARNING, "Player " + player.getUUID() + " is already registered....");
        else
            players.add(player);
    }

    public Optional<ProxyPlayer> getPlayer(UUID uuid) {
        return players.stream().filter(p -> p.getUUID().equals(uuid)).findFirst();
    }

    public CopyOnWriteArrayList<ProxyPlayer> getPlayers() {
        return players;
    }

    public void removePlayer(ProxyPlayer player) {
        players.remove(player);
    }

    public IneundoManager getManager() {
        return manager;
    }
}
