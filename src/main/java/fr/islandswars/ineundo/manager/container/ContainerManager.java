package fr.islandswars.ineundo.manager.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.LogConfig;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.secrets.DockerSecretsLoader;
import fr.islandswars.commons.service.ServiceType;
import fr.islandswars.commons.service.docker.ContainerType;
import fr.islandswars.commons.service.docker.DockerConnection;
import fr.islandswars.commons.service.rabbitmq.packet.proxy.ContainerUpPacket;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.utils.ProxyConstants;
import fr.islandswars.ineundo.utils.RedisConstants;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * File <b>ContainerManager</b> located on fr.islandswars.ineundo.manager.container
 * ContainerManager is a part of ineundo.
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
 * Created the 07/07/2024 at 19:02
 * @since 0.1
 */
public class ContainerManager {

    private final DockerClient                       dockerClient;
    private final RedisAsyncCommands<String, String> redis;
    private final String                             velocitySecret;
    private final HostConfig                         hostConfig;
    private final IslandsLogger                      logger;
    private final String[]                           secrets;

    public ContainerManager(DockerConnection dockerConnection, RedisAsyncCommands<String, String> redis, String velocitySecret) {
        this.dockerClient = dockerConnection.getConnection();
        this.velocitySecret = velocitySecret;
        this.redis = redis;
        this.logger = IslandsLogger.getLogger();
        this.hostConfig = HostConfig.newHostConfig()
                .withNetworkMode("bridge")
                .withLogConfig(new LogConfig().setType(LogConfig.LoggingType.SYSLOG));
        this.secrets = loadSecrets();
    }

    public void start(ContainerType type) {
        var container = new Container(type, ContainerImage.getImage(type));
        CompletableFuture<CreateContainerResponse> containerFuture = CompletableFuture.supplyAsync(() -> {
            var env = Arrays.copyOf(secrets, secrets.length + 4);
            env[secrets.length] = ProxyConstants.PROXY_SECRET_KEY + "=" + velocitySecret;
            env[secrets.length + 1] = "SERVER_TYPE=" + type.name();
            env[secrets.length + 2] = "SERVER_ID=" + container.getContainerID();
            env[secrets.length + 3] = "COMPOSE=true";
            return dockerClient.createContainerCmd(container.getImageID())
                    .withName(container.getContainerName())
                    .withEnv(env)
                    .withHostConfig(hostConfig).exec();
        });
        containerFuture.thenApplyAsync(re -> {
            dockerClient.connectToNetworkCmd().withContainerId(re.getId()).withNetworkId("islands_dev_network").exec();
            dockerClient.startContainerCmd(re.getId()).exec();
            return re;
        }).whenCompleteAsync((re, th) -> {
            if (th != null) {
                th.printStackTrace();
                logger.logError(new IneundoError("Canno't start the container.", th));
            } else {
                var op1 = redis.set(RedisConstants.SERVER_NAME(container.getContainerID()), container.getContainerName()).toCompletableFuture();
                var op2 = redis.set(RedisConstants.SERVER_TYPE(container.getContainerID()), type.name()).toCompletableFuture();
                var op3 = redis.set(RedisConstants.SERVER_PLAYER_COUNT(container.getContainerID()), "0").toCompletableFuture();
                CompletableFuture.allOf(op1, op2, op3).whenCompleteAsync((r, thr) -> {
                    if (thr != null)
                        logger.logError(new IneundoError("Canno't set container data in redis"));

                    Ineundo.getInstance().sendPacketToProxies(new ContainerUpPacket().withContainerId(container.getContainerID()).withProxyId(Ineundo.getInstance().getProxyId()));
                });

            }
        });
    }

    public void stop() {

    }

    //TODO remove or add debug spec
    private String[] loadSecrets() {
        var      values  = ServiceType.cachedValues();
        String[] secrets = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            secrets[i] = values[i].getSecretFileName() + "=" + DockerSecretsLoader.getValue(values[i]);
        }
        return secrets;
    }

}
