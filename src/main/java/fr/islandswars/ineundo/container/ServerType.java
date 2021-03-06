package fr.islandswars.ineundo.container;

import fr.islandswars.commons.service.Key;

/**
 * File <b>ServerType</b> located on fr.islandswars.ineundo.container
 * ServerType is a part of ineundo.
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
 * Created the 06/03/2021 at 16:00
 * @since 0.1
 */
public enum ServerType implements Key {

	HUB("hub", 0.1),
	PROXY("ineundo", 0.1),
	ISLANDS("islands", 0.1);

	private static final String prefix = "is";
	private final        String serverName;
	private final        double version;

	ServerType(String serverName, double verison) {
		this.version = verison;
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	@Override
	public String getKey() {
		return prefix + "-" + serverName + ":" + version;
	}
}
