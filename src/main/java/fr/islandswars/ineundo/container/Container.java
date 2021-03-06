package fr.islandswars.ineundo.container;

import fr.islandswars.commons.service.docker.DockerContainer;
import fr.islandswars.ineundo.Ineundo;

/**
 * File <b>Container</b> located on fr.islandswars.ineundo.container
 * Container is a part of ineundo.
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
 * Created the 03/03/2021 at 17:30
 * @since 0.1
 */
public abstract class Container {

	private final DockerContainer container;
	private final ServerType      type;
	private final int             id;

	public Container(DockerContainer container, ServerType type, int id) {
		this.container = container;
		this.type = type;
		this.id = id;
		container.withName(type.getServerName() + "-" + String.format("%04d", id));
	}

	public int getId() {
		return id;
	}

	public ServerType getType() {
		return type;
	}

	public void start() {
		container.start(Ineundo.getInstance().getContainerManager().getService());
	}
}
