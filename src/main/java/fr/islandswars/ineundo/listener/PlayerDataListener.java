package fr.islandswars.ineundo.listener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import fr.islandswars.commons.service.collection.Collection;
import fr.islandswars.commons.service.mongodb.FutureSubscriber;
import fr.islandswars.commons.service.mongodb.MongoDBConnection;
import fr.islandswars.commons.service.mongodb.ObservableSubscriber;
import fr.islandswars.commons.service.mongodb.OperationSubscriber;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.player.IslandsPlayer;
import net.kyori.adventure.text.Component;
import org.bson.Document;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * File <b>PlayerDataListener</b> located on fr.islandswars.ineundo.listener
 * PlayerDataListener is a part of ineundo.
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
 * Created the 24/06/2024 at 16:10
 * @since 0.1
 */
public class PlayerDataListener extends LazyListener {

    private final long                                    mongoTimeout     = 2L;
    private final TimeUnit                                mongoTimeoutUnit = TimeUnit.SECONDS;
    private final List<OperationSubscriber<UpdateResult>> pendingResults;
    private final Collection<IslandsPlayer>               playersCollection;

    public PlayerDataListener(Ineundo ineundo, MongoDBConnection connection) {
        super(ineundo);
        this.pendingResults = new CopyOnWriteArrayList<>();
        this.playersCollection = connection.getCollection("players", IslandsPlayer.class);
    }

    @Subscribe
    public void onLogin(PreLoginEvent event) {
        var uuid = event.getUniqueId();
        fetchPlayerData(uuid);
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onServerPreconnectEvent(ServerPreConnectEvent event) {
        if (event.getPreviousServer() == null) {
            return EventTask.async(() -> {
                getPlayerAsync(event.getPlayer().getUniqueId()).whenCompleteAsync((optPlayer, ex) -> {
                    if (ex != null || optPlayer.isEmpty()) {
                        ex.printStackTrace();
                        event.getPlayer().disconnect(Component.text("Database issue"));
                    } else {
                        var player = optPlayer.get();
                        //TODO server for staff only
                        //TODO server offline
                        //TODO player banned
                        //TODO set on redis
                    }
                });
            });
        }
        return null;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        var uuid      = event.getPlayer().getUniqueId();
        var optPlayer = getPlayer(uuid);
        //TODO fetch from redis
        log("quit event call ");
        optPlayer.ifPresent(this::savePlayerData);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        while (!pendingResults.isEmpty()) {
        }
    }

    private void savePlayerData(IslandsPlayer player) {
        var subscriber = playersCollection.replace(player, Filters.eq("uuid", player.getUUID().toString()));
        pendingResults.add(subscriber);

        CompletableFuture<UpdateResult> result = new CompletableFuture<>();
        result.completeAsync(subscriber::first);
        result.whenCompleteAsync((re, th) -> {
            if (th != null)
                th.printStackTrace();
            getIneundo().removePlayer(player);
            pendingResults.remove(subscriber);
        });
    }

    private void fetchPlayerData(UUID uuid) {
        var publisher = playersCollection.findOne(Filters.eq("uuid", uuid.toString()));
        publisher.thenApplyAsync(player -> {
                    if (player == null) {
                        player = new IslandsPlayer();
                        player.setUUID(uuid);
                    } else {
                        player.setLastConnection();
                    }
                    return player;
                })
                .thenAcceptAsync(player -> getIneundo().addPlayer(player))
                .orTimeout(mongoTimeout, mongoTimeoutUnit)
                .exceptionallyAsync(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<Optional<IslandsPlayer>> getPlayerAsync(UUID uuid) {
        CompletableFuture<Optional<IslandsPlayer>> future = new CompletableFuture<>();

        var scheduledTask = getServer().getScheduler().buildTask(getIneundo(), () -> {
            var player = getPlayer(uuid);
            if (player.isPresent())
                future.complete(player);
        }).repeat(Duration.of(100, ChronoUnit.MILLIS)).schedule();

        future.orTimeout(mongoTimeout, mongoTimeoutUnit).whenComplete((optPlayer, ex) -> {
            scheduledTask.cancel();
            if (ex != null) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }
}
