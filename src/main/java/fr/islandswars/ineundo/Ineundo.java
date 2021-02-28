package fr.islandswars.ineundo;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import fr.islandswars.commons.service.mongodb.MongoDBService;
import fr.islandswars.ineundo.listener.ServerListener;
import fr.islandswars.ineundo.player.IslandsPlayer;
import fr.islandswars.ineundo.player.i18n.Translatable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

	private static Ineundo                    instance;
	private final  Map<UUID, IslandsPlayer>   players;
	private final  MongoDBService             mongo;
	private final  ProxyServer                server;
	private final  Logger                     logger;
	private final  Path                       dataDirectory;
	private final  MinecraftChannelIdentifier channel;
	private final  Translatable               translatable;

	@Inject
	public Ineundo(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		instance = this;
		this.server = server;
		this.logger = logger;
		this.dataDirectory = dataDirectory;
		this.translatable = new Translatable();
		this.channel = MinecraftChannelIdentifier.from("core:is");
		this.mongo = new MongoDBService();
		this.players = new ConcurrentHashMap<>();
	}

	public static Ineundo getInstance() {
		return instance;
	}

	public Translatable getTranslatable() {
		return translatable;
	}

	public Optional<IslandsPlayer> getPlayer(UUID uuid) {
		return Optional.ofNullable(players.get(uuid));
	}

	public void removePlayer(UUID uuid) {
		players.remove(uuid);
	}

	public MongoDBService getMongo() {
		return mongo;
	}

	public ProxyServer getServer() {
		return server;
	}

	public Collection<IslandsPlayer> getPlayers() {
		return players.values();
	}

	public void update(IslandsPlayer player) {
		players.replace(player.getId(), player);
	}

	@Subscribe
	public void onInitialize(ProxyInitializeEvent event) {
		try {
			translatable.getLoader().registerCustomProperties(this, dataDirectory);
			//mongo.load(DockerSecretsLoader.load(ServiceType.MONGODB));
			//mongo.connect();
			//connection is async, maybe add delay before we accept connection in order to prevent database not ready state
			server.getChannelRegistrar().register(getChannel());
			//server.getEventManager().register(this, new PlayerListener(server, mongo));
			server.getEventManager().register(this, new ServerListener(server));

			/*server.getScheduler().buildTask(this, () -> {
				var cmd = new Document("ping", 1);
				mongo.getConnection().runCommand(cmd, (document, throwable) -> {
					if (throwable != null) {
						throwable.printStackTrace();
						server.shutdown();
					}
				});
			}).delay(2L, TimeUnit.SECONDS).schedule();*/
		} catch (Exception e) {
			e.printStackTrace();
			server.shutdown();
		}
	}

	public MinecraftChannelIdentifier getChannel() {
		return channel;
	}
}
