package fr.islandswars.ineundo.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.islandswars.commons.threads.ThreadUtils;
import fr.islandswars.ineundo.Ineundo;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;

/**
 * File <b>ServerListener</b> located on fr.islandswars.ineundo.listener
 * ServerListener is a part of ineundo.
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
 * Created the 28/02/2021 at 11:02
 * @since 0.1
 */
public class ServerListener {

	private final ProxyServer server;

	public ServerListener(ProxyServer server) {
		this.server = server;
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {
		server.getAllPlayers().forEach(p -> p.disconnect(Component.text("server closed")));
		Runtime.getRuntime().addShutdownHook(new Thread(this::close));
	}

	/**
	 * TODO stress test
	 */
	private void close() {
		try {
			ThreadUtils.getExecutor().shutdown();
			//the pool await for 60 seconds before killing thread
			var result = ThreadUtils.getExecutor().awaitTermination(62, TimeUnit.SECONDS);
			if (!result) {
				throw new IllegalThreadStateException("Cannot close pool properly!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
