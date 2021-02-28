package fr.islandswars.ineundo.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChannelRegisterEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.islandswars.commons.service.collection.Collection;
import fr.islandswars.commons.service.mongodb.MongoDBService;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.event.PlayerDataFetchEvent;
import fr.islandswars.ineundo.player.IslandsPlayer;
import fr.islandswars.ineundo.server.State;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import static com.mongodb.client.model.Filters.eq;

/**
 * File <b>PlayerListener</b> located on fr.islandswars.ineundo.listener
 * PlayerListener is a part of ineundo.
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
 * Created the 24/02/2021 at 09:05
 * @since 0.1
 * <p>
 * TODO handle properly container state and number of player
 */
public class PlayerListener {

	private final    Collection<IslandsPlayer> mongoPlayer;
	private final    ProxyServer               server;
	private final    Map<UUID, Status>         pendingConnections;
	private final    Gson                      gson;
	private volatile State                     serverState;
	private          long                      boot;

	public PlayerListener(ProxyServer server, MongoDBService service) {
		this.mongoPlayer = service.get("player", IslandsPlayer.class);
		this.pendingConnections = new ConcurrentHashMap<>();
		this.boot = System.currentTimeMillis() + 1000 * 5; //5 seconds to let time to the database to connect
		this.serverState = State.JOIN; //TODO start infra only for staff support in docker args
		this.gson = new Gson();
		this.server = server;
	}

	/**
	 * This event is fired when a player has initiated a connection with the proxy but before the proxy authenticates
	 * the player with Mojang or before the player's proxy connection is fully established.
	 *
	 * @param event velocity event to handle ip and profile
	 */
	@Subscribe
	public void onPlayerPreLogin(PreLoginEvent event) {
		//update proxy login-ratelimit in case of stress
		if (System.currentTimeMillis() < boot)
			event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Loading database...")));
	}

	/**
	 * This event is fired once the player has been authenticated but before they connect to a server on the proxy.
	 *
	 * @param event velocity event to handle player connection
	 */
	@Subscribe
	public void onPlayerLogin(LoginEvent event) {
		//this player is in online mode and the database are initialised
		var player = event.getPlayer();
		pendingConnections.put(player.getUniqueId(), Status.JOIN);
		server.getScheduler().buildTask(Ineundo.getInstance(), () -> mongoPlayer.find(eq("uuid", player.getUniqueId()), 1, (result, th) -> {
			server.getEventManager().fire(new PlayerDataFetchEvent(result.isEmpty() ? new IslandsPlayer(player) : result.get(0)));
		})).schedule();
	}

	@Subscribe
	public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
		//TODO dispatch to an available hub, overcrowd or kick?
	}

	/**
	 * This event is fired once the player has successfully connected to the target server and the connection to the previous server has been de-established.
	 *
	 * @param event velocity event to handle player server connection
	 */
	@Subscribe
	public void onPlayerConnect(ServerPostConnectEvent event) {
		System.out.println(event);
		var uuid = event.getPlayer().getUniqueId();
		if (pendingConnections.containsKey(uuid)) {
			//first time this player is joining a server
			var state = pendingConnections.get(uuid);
			if (state != Status.HANDLED) {
				//give some time to fetch data from the database
				server.getScheduler().buildTask(Ineundo.getInstance(), () -> {
					if (pendingConnections.containsKey(uuid)) {
						//unable to fetch data, kick the player
						server.getPlayer(uuid).ifPresent(player -> player.disconnect(Component.text(Ineundo.getInstance().getTranslatable().format("unable_fetch_data"))));
					} else {
						//data processed, check if the player hasn't been kicked, start plugin channel since he is connected
						var player = Ineundo.getInstance().getPlayer(uuid);
						player.ifPresent(p -> {
							var json = gson.toJson(p);
							sendData(event.getPlayer(), json);
						});
					}
					pendingConnections.remove(uuid);
				}).delay(2, TimeUnit.SECONDS);
			} else {
				//data processed and player allowed, start plugin channel since the player is connected
				var player = Ineundo.getInstance().getPlayer(uuid);
				player.ifPresent(p -> {
					var json = gson.toJson(p);
					sendData(event.getPlayer(), json);
				});
				pendingConnections.remove(uuid);
			}
		} else {
			var player = Ineundo.getInstance().getPlayer(uuid);
			player.ifPresent(p -> {
				var json = gson.toJson(p);
				sendData(event.getPlayer(), json);
			});
		}
	}

	@Subscribe
	public void onPlayerQuit(DisconnectEvent event) {
		var player   = event.getPlayer();
		var isPlayer = Ineundo.getInstance().getPlayer(player.getUniqueId());
		if (isPlayer.isPresent()) {
			var filter = eq("uuid", player.getUniqueId());
			mongoPlayer.putOrUpdate(filter, isPlayer.get(), (updateResult, throwable) -> {
				if (throwable != null) {
					throwable.printStackTrace();
					System.out.println(gson.toJson(isPlayer.get())); //TODO handle database offline properly
				}
			});
		}
		pendingConnections.remove(player.getUniqueId());
		isPlayer.ifPresent(p -> Ineundo.getInstance().removePlayer(p.getId()));
	}

	@Subscribe
	public void test(PlayerChannelRegisterEvent event) {
		System.out.println(event);
	}

	@Subscribe
	public void onPluginMessage(PluginMessageEvent event) {
		if (event.getIdentifier().equals(Ineundo.getInstance().getChannel())) {
			var data       = event.dataAsDataStream();
			var playerName = data.readUTF();
			var size       = data.readShort();
			var buffer     = new byte[size];
			data.readFully(buffer);
			var playerInputData = ByteStreams.newDataInput(buffer);
			var json            = playerInputData.readUTF();
			var player          = server.getPlayer(playerName).orElseThrow();
			var optIsPlayer     = Ineundo.getInstance().getPlayer(player.getUniqueId());
			optIsPlayer.ifPresent(p -> {
				var newPlayer = gson.fromJson(json, IslandsPlayer.class);
				Ineundo.getInstance().update(p);
			});
			event.setResult(PluginMessageEvent.ForwardResult.handled());
		}
	}

	/**
	 * Event fired between LoginEvent to ServerConnectedEvent, at this time, the player exist in ProxyServer's player list
	 *
	 * @param event custom event fired when data fetch on mongo is done
	 */
	@Subscribe
	public void onPlayerDataFetched(PlayerDataFetchEvent event) {
		var player = event.getPlayer();
		if (pendingConnections.containsKey(player.getId())) {
			pendingConnections.replace(player.getId(), Status.FETCHED);

			//check server states AND ban / kick
			if (serverState == State.STAFF && !player.isStaff()) {
				kickPlayer(player.getId(), Ineundo.getInstance().getTranslatable().format("player_join_not_staff"));
			}
			var optSanction = player.isBanned();
			optSanction.ifPresent(sanction -> {
				kickPlayer(player.getId(), Ineundo.getInstance().getTranslatable().format("player_join_banned", sanction.getType(), sanction.getAuthor(), new Date(sanction.getSanctionEndTime())));
			});
			player.depucelage();
			pendingConnections.replace(player.getId(), Status.HANDLED);
			Ineundo.getInstance().getPlayers().add(player);
		} else {
			//error in logic above, cancel connection
			kickPlayer(player.getId(), Ineundo.getInstance().getTranslatable().format("unable_fetch_data"));
		}
	}

	private void sendData(Player player, String json) {
		try {
			var data = Ineundo.getInstance().getPlayers().stream().filter(p -> p.getId().equals(player.getUniqueId())).findFirst();
			data.ifPresent(p -> {
				try {
					ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF(player.getUsername());

					ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
					DataOutputStream      msgout   = new DataOutputStream(msgbytes);
					msgout.writeUTF(json);
					out.writeShort(msgbytes.toByteArray().length);
					out.write(msgbytes.toByteArray());

					player.getCurrentServer().get().sendPluginMessage(Ineundo.getInstance().getChannel(), out.toByteArray());
				} catch (Exception e) {
					e.printStackTrace();
					kickPlayer(player.getUniqueId(), Ineundo.getInstance().getTranslatable().format("channel_issue", player.getUsername(), new Date().toString()));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void kickPlayer(UUID playerId, String reason) {
		server.getPlayer(playerId).ifPresent(p -> p.disconnect(Component.text(reason)));
		pendingConnections.remove(playerId);
	}

	private enum Status {
		JOIN,
		FETCHED,
		HANDLED
	}
}
