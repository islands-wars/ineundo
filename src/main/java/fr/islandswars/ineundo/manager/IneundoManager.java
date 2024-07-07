package fr.islandswars.ineundo.manager;

import fr.islandswars.commons.service.rabbitmq.RabbitMQConnection;
import fr.islandswars.commons.service.redis.RedisConnection;
import fr.islandswars.ineundo.manager.container.ContainerManager;

import java.util.concurrent.ExecutionException;

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
public class IneundoManager {

    private final RedisConnection  redis;
    private final ProxyManager     proxyManager;
    private final ContainerManager containerManager;
    private final IslandsExchange  exchange;

    public IneundoManager(RedisConnection redis, RabbitMQConnection rabbit) {
        this.redis = redis;
        this.proxyManager = new ProxyManager(redis);
        this.containerManager = new ContainerManager();
        this.exchange = new IslandsExchange(rabbit, proxyManager.getProxyId());
    }

    public void initialize() {
        proxyManager.registerProxy(exchange);
    }

    public void shutdown() throws ExecutionException, InterruptedException {
        proxyManager.shutdown(exchange);
    }
}
