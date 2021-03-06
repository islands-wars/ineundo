package fr.islandswars.ineundo.container;

import fr.islandswars.commons.service.docker.DockerContainer;

/**
 * File <b>PaperContainer</b> located on fr.islandswars.ineundo.container
 * PaperContainer is a part of ineundo.
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
public class PaperContainer extends Container {

	public PaperContainer(DockerContainer container, ServerType type, int id) {
		super(container, type, id);
	}
}
