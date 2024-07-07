package fr.islandswars.ineundo.manager;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import fr.islandswars.commons.service.rabbitmq.RabbitMQConnection;
import fr.islandswars.commons.service.rabbitmq.packet.Packet;
import fr.islandswars.commons.service.rabbitmq.packet.PacketManager;
import fr.islandswars.commons.service.rabbitmq.packet.PacketType;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.log.InternalLogger;
import fr.islandswars.ineundo.utils.RabbitConstants;

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
    private final InternalLogger      logger;
    private final UUID                proxyId;

    public IslandsExchange(RabbitMQConnection connection, UUID proxyId) {
        this.PACKET_MANAGER = new PacketManager(PacketType.Bound.MANAGER, 1024, true);
        this.logger = Ineundo.getInstance().getInfraLogger();
        this.connection = connection;
        this.proxyId = proxyId;
        this.channel = connection.getConnection();
        try {
            channel.exchangeDeclare(RabbitConstants.EXCHANGE, EXCHANGE_TYPE);
        } catch (Exception e) {
            logger.logError(e);
        }
        //new ExchangePacket(PACKET_MANAGER, server, this);
        initConnection();
    }

    public void sendPacketToProxies(Packet packet) {
        try {
            var buffer = PACKET_MANAGER.encode(packet);
            sendPacket(RabbitConstants.getProxiesQueue(), buffer);
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
            channel.queueBind(queue, RabbitConstants.EXCHANGE, queue); //listen to specific message
            channel.queueBind(queue, RabbitConstants.EXCHANGE, RabbitConstants.getProxiesQueue()); //listen to all proxies message

            channel.basicConsume(queue, true, (tag, delivery) -> {
                try {
                    PACKET_MANAGER.decode(delivery.getBody());
                } catch (Exception e) {
                    logger.logError(e);
                }
                //TODO remove
                logger.logInfo(delivery.getEnvelope().toString());
            }, consumerTag -> {
            });
        } catch (IOException e) {
            logger.logError(e);
        }
    }
}

