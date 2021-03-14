package fr.islandswars.ineundo.container;

import com.velocitypowered.api.proxy.Player;
import fr.islandswars.commons.secrets.DockerSecretsLoader;
import fr.islandswars.commons.service.ServiceType;
import fr.islandswars.commons.service.docker.DockerService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * File <b>ContainerManager</b> located on fr.islandswars.ineundo.container
 * ContainerManager is a part of ineundo.
 * <p>
 * Copyright (c) 2017 - 2021 Islands Wars.
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
 * @author Valentin Burgaud (Xharos), {@literal <xharos@islandswars.fr>}
 * Created the 03/03/2021 at 17:31
 * @since 0.1
 */
public class ContainerManager implements Runnable {

	private static final int             SWAP_LIMIT = 50;
	private final        DockerService   service;
	private final        List<Container> containers;

	public ContainerManager() {
		this.service = new DockerService();
		this.containers = new CopyOnWriteArrayList<>();
	}

	public void connect() throws Exception {
		service.load(DockerSecretsLoader.load(ServiceType.DOCKER));
		service.connect();
		service.getConnection().pingCmd().exec();
	}

	public Container createContainer(ServerType type) {
		var dockerContainer = service.createContainer(type);
		var id              = getFreeId(type);
		var container       = new PaperContainer(dockerContainer, type, id);
		containers.add(container);
		return container;
	}

	public DockerService getService() {
		return service;
	}

	public int getSwap(ServerType type) {
		return containers.stream().filter(container -> container.getType().equals(type)).filter(c -> c.getStatus().equals(Status.RUNNING))
				.map(c -> c.getMaxAllowedPlayer() - c.getOnlinePlayers() - c.getPendingPlayers()).reduce(0, Integer::sum);
	}

	@Override
	public void run() {
		for (ServerType value : ServerType.values()) {
			//check server state and start new one if needed
			var swap = getSwap(value);
			if (swap <= SWAP_LIMIT) {
				var startedServer   = containers.stream().filter(c -> c.getType().equals(value) && c.getStatus().equals(Status.STARTING)).count();
				var startedCapacity = value.getMaxPlayer() * startedServer;
				if (startedCapacity <= SWAP_LIMIT) {
					var serverToStart = ((int) (SWAP_LIMIT - startedCapacity) / value.getMaxPlayer()) + 1;
					for (var i = 0; i < serverToStart; i++)
						createContainer(value);
				}
			}
		}
	}

	public void connectPlayer(ServerType type, Player player) {

	}

	private int getFreeId(ServerType type) {
		var ids = containers.stream().filter(c -> c.getType().equals(type)).map(Container::getId).collect(Collectors.toSet());
		return IntStream.iterate(1, n -> n + 1).filter(n -> !ids.contains(n)).findFirst().getAsInt();
	}
}
