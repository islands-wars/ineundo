package fr.islandswars.ineundo.manager;

import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.service.docker.ContainerType;
import fr.islandswars.commons.service.rabbitmq.packet.PacketManager;
import fr.islandswars.commons.service.rabbitmq.packet.PacketType;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.event.ContainerStartEvent;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.utils.RedisConstants;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * File <b>ExchangePacketListener</b> located on fr.islandswars.ineundo.manager
 * ExchangePacketListener is a part of ineundo.
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
 * Created the 10/07/2024 at 17:03
 * @since 0.1
 */
public class ExchangePacketListener {

    private final PacketManager                      PACKET_MANAGER;
    private final RedisAsyncCommands<String, String> redis;
    private final IslandsLogger                     logger;

    public ExchangePacketListener(PacketManager PACKET_MANAGER, RedisAsyncCommands<String, String> redis, IslandsLogger logger) {
        this.PACKET_MANAGER = PACKET_MANAGER;
        this.redis = redis;
        this.logger = logger;
        addListener();
    }

    private void addListener() {
        PACKET_MANAGER.addListener(PacketType.Status.CONTAINER_UP_REQUEST, (event) -> {
            var containerName = redis.get(RedisConstants.SERVER_NAME(event.getContainerId())).toCompletableFuture();
            var containerType = redis.get(RedisConstants.SERVER_TYPE(event.getContainerId())).toCompletableFuture();
            CompletableFuture.allOf(containerType, containerName).whenCompleteAsync((re, th) -> {
                if (th != null)
                    logger.logError(new IneundoError("Canno't retrieve container data"));
                try {
                    var name = containerName.get();
                    var type = containerType.get();
                    Ineundo.getInstance().getServer().getEventManager().fire(new ContainerStartEvent(event.getContainerId(), ContainerType.valueOf(type), name));
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
