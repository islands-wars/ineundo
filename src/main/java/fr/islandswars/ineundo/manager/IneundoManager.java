package fr.islandswars.ineundo.manager;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.scheduler.ScheduledTask;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.service.docker.ContainerType;
import fr.islandswars.commons.service.docker.DockerConnection;
import fr.islandswars.commons.service.rabbitmq.RabbitMQConnection;
import fr.islandswars.commons.service.rabbitmq.packet.Packet;
import fr.islandswars.commons.service.rabbitmq.packet.server.StatusRequestPacket;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.event.ContainerEnableEvent;
import fr.islandswars.ineundo.event.ContainerStartEvent;
import fr.islandswars.ineundo.event.ContainerStopEvent;
import fr.islandswars.ineundo.listener.LazyListener;
import fr.islandswars.ineundo.manager.container.ContainerManager;
import fr.islandswars.ineundo.manager.container.ServerCache;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * File <b>IneundoManager</b> located on fr.islandswars.ineundo.manager
 * IneundoManager is a part of ineundo.
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
 * Created the 07/07/2024 at 18:32
 * @since 0.1
 */
public class IneundoManager extends LazyListener {

    private final ProxyManager                       proxyManager;
    private final ContainerManager                   containerManager;
    private final IslandsExchange                    exchange;
    private final Ineundo                            ineundo;
    private final IslandsLogger                      logger;
    private final RedisAsyncCommands<String, String> redis;
    private final Integer                            THRESHOLD;
    private final List<ServerCache>                  servers;
    private       ScheduledTask                      updateTask;

    public IneundoManager(Ineundo ineundo, RedisAsyncCommands<String, String> redis, RabbitMQConnection rabbit, DockerConnection docker, String velocitySecret) {
        super(ineundo);
        this.redis = redis;
        this.logger = IslandsLogger.getLogger();
        this.proxyManager = new ProxyManager(redis);
        this.containerManager = new ContainerManager(docker, redis, velocitySecret);
        this.exchange = new IslandsExchange(rabbit, redis);
        this.ineundo = Ineundo.getInstance();
        this.THRESHOLD = 10;
        this.servers = new CopyOnWriteArrayList<>();
    }

    public void initialize() {
        proxyManager.registerProxy(exchange);
        proxyManager.registerServers();
        this.updateTask = ineundo.getServer().getScheduler().buildTask(ineundo, updatesLogic()).repeat(1000, TimeUnit.MILLISECONDS).delay(1, TimeUnit.SECONDS).schedule();
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        updateTask.cancel();
        for (ServerCache server : servers) {
            server.stopUpdate();
        }
        proxyManager.shutdown(exchange);
    }

    @Subscribe
    public void onContainerStart(ContainerStartEvent event) {
        logger.logInfo("StartContainerEvent for " + event.containerName());
        //load is normal behaviour
        //enable is that a proxy has been started when the server is already running
        if (event.status() == StatusRequestPacket.ServerStatus.LOAD || event.status() == StatusRequestPacket.ServerStatus.ENABLE) {
            servers.add(new ServerCache(redis, event));
            if (event.status() == StatusRequestPacket.ServerStatus.ENABLE)
                ineundo.getServer().getEventManager().fire(new ContainerEnableEvent(event.containerId()));
        } else {
            logger.log(Level.WARNING, "Try to register a server that is closing or stopping");
            //TODO remove it ?
        }
    }

    @Subscribe
    public void onContainerReady(ContainerEnableEvent event) {
        logger.logInfo("Container " + event.containerId() + " is now enable.");
        getCache(event.containerId()).ifPresent(serverCache -> {
            serverCache.setStatus(StatusRequestPacket.ServerStatus.ENABLE);
            ineundo.getServer().registerServer(serverCache.getServerInfo());
            logger.logDebug("Container " + event.containerId() + " is now registered in local server map.");
        });
    }

    @Subscribe
    public void onContainerStop(ContainerStopEvent event) {
        logger.logInfo("StopContainerEvent for " + event.containerId());
        getCache(event.containerId()).ifPresent(serverCache -> {
            ineundo.getServer().unregisterServer(serverCache.getServerInfo());
            servers.remove(serverCache);
            serverCache.clean();
            containerManager.delete(serverCache.getName());
        });
    }

    public void sendPacket(Packet packet, String routingKey) {
        exchange.sendPacket(packet, routingKey);
    }

    private Optional<ServerCache> getCache(UUID containerId) {
        ServerCache serverCache = null;
        for (ServerCache server : servers) {
            if (server.getServerId().equals(containerId))
                serverCache = server;
        }
        return Optional.ofNullable(serverCache);
    }

    private Runnable updatesLogic() {
        return () -> {
            var playerCount     = ineundo.getPlayers().size(); // Get the number of players connected to this proxy
            int totalPlayerLoad = playerCount * proxyManager.getOnlineProxies(); // Calculate the total player load across all proxies
            for (ContainerType type : ContainerType.cachedValues()) {

                int currentCapacity = getCapacity(type); // Get the current player capacity for this container type
                int loadDifference  = currentCapacity - totalPlayerLoad; // Calculate the load difference for the given container type
                if (loadDifference < THRESHOLD) {
                    int numberOfNeededContainers = Math.abs((int) Math.ceil((double) (THRESHOLD - loadDifference) / type.getMaxPlayerCount()));
                    for (int i = 0; i < numberOfNeededContainers; i++) {
                        logger.logDebug("Attempt to start a new container " + type + " because the player threshold is too low");
                        containerManager.start(type);
                    }
                }
            }
        };
    }

    private int getCapacity(ContainerType type) {
        int capacity = 0;
        for (ServerCache server : servers) {
            if (server.getType() == type)
                capacity += type.getMaxPlayerCount();
        }
        return capacity;
    }
}
