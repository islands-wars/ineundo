package fr.islandswars.ineundo.manager;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.service.rabbitmq.RabbitMQConnection;
import fr.islandswars.commons.service.rabbitmq.packet.Packet;
import fr.islandswars.commons.service.rabbitmq.packet.PacketManager;
import fr.islandswars.commons.service.rabbitmq.packet.PacketType;
import fr.islandswars.ineundo.utils.RabbitConstants;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.io.IOException;
import java.util.UUID;

/**
 * File <b>IslandsExchange</b> located on fr.islandswars.ineundo.manager
 * IslandsExchange is a part of ineundo.
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
 * Created the 07/07/2024 at 21:40
 * @since 0.1
 */
public class IslandsExchange {

    private final BuiltinExchangeType EXCHANGE_TYPE = BuiltinExchangeType.TOPIC;
    private final PacketManager       PACKET_MANAGER;
    private final RabbitMQConnection  connection;
    private final Channel             channel;
    private final IslandsLogger      logger;
    private final UUID                proxyId;

    public IslandsExchange(RabbitMQConnection connection, RedisAsyncCommands<String, String> redis, UUID proxyId) {
        this.PACKET_MANAGER = new PacketManager(PacketType.Bound.MANAGER, 1024, true);
        this.logger = IslandsLogger.getLogger();
        this.connection = connection;
        this.proxyId = proxyId;
        this.channel = connection.getConnection();
        new ExchangePacketListener(PACKET_MANAGER, redis, logger);
        initConnection();
    }

    public void sendPacket(Packet packet, String routingKey) {
        try {
            var buffer = PACKET_MANAGER.encode(packet);
            sendPacket(routingKey, buffer);
        } catch (Exception e) {
            logger.logError(e);
        }
    }

    private void sendPacket(String routingKey, byte[] packet) {
        try {
            channel.basicPublish(RabbitConstants.EXCHANGE, routingKey, null, packet);
        } catch (Exception e) {
            logger.logError(e);
        }
    }

    private void initConnection() {
        var channel = connection.getConnection();
        try {
            var queue = RabbitConstants.getProxyQueue(proxyId);
            channel.exchangeDeclare(RabbitConstants.EXCHANGE, EXCHANGE_TYPE);
            channel.queueDeclare(queue, false, true, false, null);
            channel.queueBind(queue, RabbitConstants.EXCHANGE, queue); //listen to specific message proxy.uuid
            channel.queueBind(queue, RabbitConstants.EXCHANGE, RabbitConstants.getProxiesQueue()); //listen to all proxies message proxy.all

            channel.basicConsume(queue, true, (tag, delivery) -> {
                try {
                    PACKET_MANAGER.decode(delivery.getBody());
                } catch (Exception e) {
                    logger.logError(e);
                }
                //TODO remove
            }, consumerTag -> {
            });
        } catch (IOException e) {
            logger.logError(e);
        }
    }
}

