package fr.islandswars.ineundo.manager;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.scheduler.ScheduledTask;
import fr.islandswars.commons.service.docker.DockerConnection;
import fr.islandswars.commons.service.rabbitmq.RabbitMQConnection;
import fr.islandswars.commons.service.redis.RedisConnection;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.event.ContainerStartEvent;
import fr.islandswars.ineundo.listener.LazyListener;
import fr.islandswars.ineundo.manager.container.ContainerManager;
import fr.islandswars.ineundo.manager.container.ContainerType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

    private final RedisConnection             redis;
    private final ProxyManager                proxyManager;
    private final ContainerManager            containerManager;
    private final IslandsExchange             exchange;
    private final Ineundo                     ineundo;
    private final Integer                     THRESHOLD;
    private final Map<ContainerType, Integer> futureCapacity;
    private       ScheduledTask               updateTask;

    public IneundoManager(Ineundo ineundo, RedisConnection redis, RabbitMQConnection rabbit, DockerConnection docker, String velocitySecret) {
        super(ineundo);
        this.redis = redis;
        this.proxyManager = new ProxyManager(redis);
        this.containerManager = new ContainerManager(docker, velocitySecret);
        this.exchange = new IslandsExchange(rabbit, proxyManager.getProxyId());
        this.ineundo = Ineundo.getInstance();
        this.THRESHOLD = 10;
        this.futureCapacity = new ConcurrentHashMap<>();
        for (ContainerType type : ContainerType.values()) {
            futureCapacity.put(type, 0);
        }
    }

    public void initialize() {
        proxyManager.registerProxy(exchange);
        this.updateTask = ineundo.getServer().getScheduler().buildTask(ineundo, updatesLogic()).repeat(1, TimeUnit.SECONDS).schedule();
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        updateTask.cancel();
        proxyManager.shutdown(exchange);
    }

    private Runnable updatesLogic() {
        return () -> {
            var playerCount     = ineundo.getPlayers().size(); // Get the number of players connected to this proxy
            int totalPlayerLoad = playerCount * proxyManager.getOnlineProxies(); // Calculate the total player load across all proxies
            for (ContainerType type : ContainerType.values()) {

                int currentCapacity = futureCapacity.getOrDefault(type, 0); // Get the current player capacity for this container type
                int loadDifference  = currentCapacity - totalPlayerLoad; // Calculate the load difference for the given container type
                if (loadDifference < THRESHOLD) {
                    int numberOfNeededContainers = Math.abs((int) Math.ceil((double) (THRESHOLD - loadDifference) / type.getPlayerCount()));
                    for (int i = 0; i < numberOfNeededContainers; i++) {
                        containerManager.start(type);
                    }
                }
            }
        };
    }

    @Subscribe
    public void onContainerStart(ContainerStartEvent event) {
        Ineundo.getInstance().getInfraLogger().logInfo("Start a new " + event.type() + " container name " + event.containerName());
        futureCapacity.computeIfPresent(event.type(), (k, v) -> v + event.type().getPlayerCount());
    }
}
