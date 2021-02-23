package fr.islandswars.ineundo;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

/**
 * File <b>Ineundo</b> located on fr.islandswars.ineundo
 * Ineundo is a part of ineundo.
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
 * Created the 23/02/2021 at 16:03
 * @since 0.1
 */
@Plugin(id = "ineundo", name = "Ineundo", version = "0.1", authors = {"Xharos"})
public class Ineundo {

	private final ProxyServer server;
	private final Logger      logger;

	@Inject
	public Ineundo(ProxyServer server, Logger logger) {
		this.server = server;
		this.logger = logger;

		logger.info("Hello world !");
	}
}
