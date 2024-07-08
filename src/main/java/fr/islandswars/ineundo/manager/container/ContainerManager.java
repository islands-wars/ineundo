package fr.islandswars.ineundo.manager.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.Network;
import fr.islandswars.commons.service.docker.DockerConnection;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.log.InternalLogger;
import fr.islandswars.ineundo.utils.ProxyConstants;

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

    private final DockerClient   dockerClient;
    private final String         velocitySecret;
    private final HostConfig     hostConfig;
    private final InternalLogger logger;

    public ContainerManager(DockerConnection dockerConnection, String velocitySecret) {
        this.dockerClient = dockerConnection.getConnection();
        this.velocitySecret = velocitySecret;
        this.logger = Ineundo.getInstance().getInfraLogger();
        this.hostConfig = HostConfig.newHostConfig().withNetworkMode("bridge").withLogConfig(new LogConfig().setType(LogConfig.LoggingType.SYSLOG));
    }

    public void start(ContainerType type) {
        logger.logInfo("Attempt to create a new container " + type.name());
        var container = new Container(type);
        CompletableFuture<CreateContainerResponse> containerFuture = CompletableFuture.supplyAsync(() -> dockerClient.createContainerCmd(container.getImageID())
                .withName(container.getContainerName())
                .withEnv(ProxyConstants.PROXY_SECRET_KEY + "=" + velocitySecret)
                .withHostConfig(hostConfig).exec());
        containerFuture.thenApplyAsync(re -> {
            logger.logInfo("Started container " + type.name().toLowerCase() + "; ID=" + re.getId());
            dockerClient.connectToNetworkCmd().withContainerId(re.getId()).withNetworkId("islands_dev_network").exec();
            dockerClient.startContainerCmd(re.getId()).exec();
            return re;
        }).whenCompleteAsync((re, th) -> {
            if (th != null) {
                th.printStackTrace();
                logger.logError(new IneundoError("Canno't start the container.", th));
            } else {
                logger.logInfo("Container " + type.name().toLowerCase() + "; ID=" + re.getId() + " started on the network");
            }
        });
    }

    public void stop() {

    }

}
