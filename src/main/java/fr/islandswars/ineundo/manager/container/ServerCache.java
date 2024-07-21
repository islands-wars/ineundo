package fr.islandswars.ineundo.manager.container;

import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.service.docker.ContainerType;
import fr.islandswars.commons.service.rabbitmq.packet.server.StatusRequestPacket;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.event.ContainerStartEvent;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.utils.RedisConstants;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * File <b>ServerCache</b> located on fr.islandswars.ineundo.manager.container
 * ServerCache is a part of ineundo.
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
 * Created the 11/07/2024 at 23:27
 * @since 0.1
 */
public class ServerCache {

    private final RedisAsyncCommands<String, String> redis;
    private final UUID                               serverId;
    private final ContainerType                      type;
    private final String                             name;
    private final ServerInfo                         serverInfo;
    private final ScheduledTask                      task;
    private       StatusRequestPacket.ServerStatus   status;
    private       int                                playerCount;

    public ServerCache(RedisAsyncCommands<String, String> redis, ContainerStartEvent event) {
        this.serverId = event.containerId();
        this.type = event.type();
        this.name = event.containerName();
        this.serverInfo = new ServerInfo(name, new InetSocketAddress(name, 25565));
        this.status = event.status();
        this.redis = redis;
        this.task = Ineundo.getInstance().getServer().getScheduler().buildTask(Ineundo.getInstance(), updatePlayerCount()).repeat(5, TimeUnit.MILLISECONDS).schedule();
    }

    public void clean() {
        stopUpdate();
        var op1 = redis.del(RedisConstants.SERVER_NAME(serverId), RedisConstants.SERVER_TYPE(serverId), RedisConstants.SERVER_STATUS(serverId), RedisConstants.SERVER_PLAYER_COUNT(serverId)).toCompletableFuture();
        var op2 = redis.lrem(RedisConstants.SERVER_LISTS, 0, serverId.toString()).toCompletableFuture();
        CompletableFuture.allOf(op1, op2).whenCompleteAsync((r, th) -> {
            if (th != null)
                IslandsLogger.getLogger().logError(new IneundoError("Cannot delete server info on redis", th));
        });
    }

    private Runnable updatePlayerCount() {
        return () -> {
            redis.get(RedisConstants.SERVER_PLAYER_COUNT(serverId)).whenCompleteAsync((re, th) -> {
                if (th != null)
                    IslandsLogger.getLogger().logError(new IneundoError("Cannot retrieve player count", th));
                else
                    playerCount = Integer.parseInt(re);
            });
        };
    }

    public void stopUpdate() {
        task.cancel();
    }


    public void setStatus(StatusRequestPacket.ServerStatus status) {
        this.status = status;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public String getName() {
        return name;
    }

    public UUID getServerId() {
        return serverId;
    }

    public ContainerType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ServerCache for " + name + " ID : " + serverId + ", TYPE : " + type;
    }
}
