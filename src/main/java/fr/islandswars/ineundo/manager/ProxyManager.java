package fr.islandswars.ineundo.manager;

import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.service.rabbitmq.packet.proxy.ProxyDownPacket;
import fr.islandswars.commons.service.rabbitmq.packet.proxy.ProxyUpPacket;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.utils.RabbitConstants;
import fr.islandswars.ineundo.utils.RedisConstants;
import io.lettuce.core.api.async.RedisAsyncCommands;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

/**
 * File <b>ProxyManager</b> located on fr.islandswars.ineundo.manager
 * ProxyManager is a part of ineundo.
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
 * Created the 07/07/2024 at 18:33
 * @since 0.1
 */
public class ProxyManager {

    private final RedisAsyncCommands<String, String> redis;
    private final UUID                               PROXY_ID;
    private final IslandsLogger                      logger;
    private final CopyOnWriteArrayList<UUID>         proxies;

    public ProxyManager(RedisAsyncCommands<String, String> redis) {
        this.redis = redis;
        this.PROXY_ID = UUID.randomUUID();
        this.proxies = new CopyOnWriteArrayList<>();
        this.logger = IslandsLogger.getLogger();
    }

    public UUID getProxyId() {
        return PROXY_ID;
    }

    protected void registerProxy(IslandsExchange exchange) {
        redis.rpush(RedisConstants.PROXY, PROXY_ID.toString()).whenCompleteAsync((re, th) -> {
            if (th != null) {
                logger.logError(new IneundoError("Canno't register the proxy", th));
                Ineundo.getInstance().getServer().shutdown(Component.translatable("proxy.startup.register.error"));
            }
            exchange.sendPacket(new ProxyUpPacket().withProxyId(PROXY_ID), RabbitConstants.getProxiesQueue());
        });
        redis.lrange(RedisConstants.PROXY, 0, -1).whenCompleteAsync((re, th) -> {
            if (th != null) {
                logger.logError(new IneundoError("Canno't retrieve other proxies", th));
                Ineundo.getInstance().getServer().shutdown(Component.translatable("proxy.startup.register.error"));
            }
            for (var proxyKey : re) {
                if (!PROXY_ID.equals(UUID.fromString(proxyKey))) {
                    proxies.add(UUID.fromString(proxyKey));
                }
            }
        });
    }

    protected void shutdown(IslandsExchange exchange) throws ExecutionException, InterruptedException {
        exchange.sendPacket(new ProxyDownPacket().withProxyId(PROXY_ID), RabbitConstants.getProxiesQueue());
        redis.lrem(RedisConstants.PROXY, 0, PROXY_ID.toString()).get();
    }

    protected int getOnlineProxies() {
        return proxies.size() + 1;
    }
}
