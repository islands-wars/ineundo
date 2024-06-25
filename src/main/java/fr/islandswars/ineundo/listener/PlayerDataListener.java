package fr.islandswars.ineundo.listener;

import com.mongodb.client.result.UpdateResult;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import fr.islandswars.commons.service.collection.Collection;
import fr.islandswars.commons.service.mongodb.MongoDBConnection;
import fr.islandswars.commons.service.mongodb.OperationSubscriber;
import fr.islandswars.commons.service.redis.RedisConnection;
import fr.islandswars.commons.utils.ReflectionUtil;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.player.IslandsPlayer;
import fr.islandswars.ineundo.utils.MongoConstants;
import fr.islandswars.ineundo.utils.RedisConstants;
import net.kyori.adventure.text.Component;
import org.bson.Document;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
 * TODO proper logging
 */
public class PlayerDataListener extends LazyListener {

    private final long                                    mongoTimeout     = 2L;
    private final TimeUnit                                mongoTimeoutUnit = TimeUnit.SECONDS;
    private final List<OperationSubscriber<UpdateResult>> pendingResults;
    private final Collection<IslandsPlayer>               playersCollection;
    private final RedisConnection                         redis;

    public PlayerDataListener(Ineundo ineundo, MongoDBConnection mongo, RedisConnection redis) {
        super(ineundo);
        this.redis = redis;
        this.pendingResults = new CopyOnWriteArrayList<>();
        this.playersCollection = mongo.getCollection(MongoConstants.PLAYER_COLLECTION, IslandsPlayer.class);
    }

    @Subscribe
    public void onLogin(PreLoginEvent event) {
        var uuid = event.getUniqueId();
        fetchPlayerData(uuid);
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onServerPreconnectEvent(ServerPreConnectEvent event) {
        if (event.getPreviousServer() == null) {
            return EventTask.async(() -> synchroniseData(event));
        } else
            return null;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        var uuid      = event.getPlayer().getUniqueId();
        var optPlayer = getPlayer(uuid);
        optPlayer.ifPresent(this::savePlayerData);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        while (!pendingResults.isEmpty()) {
        }
    }

    private void savePlayerData(IslandsPlayer player) {
        updateFromRedis(player).whenCompleteAsync((p, thr) -> {
            if (thr != null)
                error(new IneundoError("Error when saving player data in MongoDB...", thr));

            var subscriber = playersCollection.replace(player, MongoConstants.PLAYER_ID_FILTER(player.getUUID()));
            pendingResults.add(subscriber);

            CompletableFuture<UpdateResult> result = new CompletableFuture<>();
            result.completeAsync(subscriber::first);
            result.whenCompleteAsync((re, th) -> {
                if (th != null)
                    error(new IneundoError("Error when saving player data in MongoDB...", th));
                getIneundo().removePlayer(player);
                pendingResults.remove(subscriber);
            });
            //TODO maybe add a timeout here in case the player wants to connect back to the server
            //TODO or check if a player with this uuid is already existing when joining
        });
    }

    private void synchroniseData(ServerPreConnectEvent event) {
        getPlayerAsync(event.getPlayer().getUniqueId()).whenCompleteAsync((optPlayer, th) -> {
            if (th != null || optPlayer.isEmpty()) {
                error(new IneundoError("Player " + event.getPlayer().getUsername() + " cannot be retrieved in time", th));
                event.getPlayer().disconnect(Component.translatable("event.join.data.error"));
            } else {
                var player   = optPlayer.get();
                var sanction = player.isKick();
                sanction.ifPresent(s -> event.getPlayer().disconnect(s.getKickMessage()));
                if (getIneundo().getSTAFF_ONLY().get() && !player.getMainRank().isStaff()) event.getPlayer().disconnect(Component.translatable("event.join.staff"));
                else {
                    //TODO server offline
                    redis.getConnection().set(RedisConstants.PLAYER_KEY(player.getUUID()), playersCollection.serialize(player).toJson());
                }
            }
        });
    }

    private CompletionStage<IslandsPlayer> updateFromRedis(IslandsPlayer current) {
        return redis.getConnection().get(current.getUUID().toString() + ":player").handleAsync((json, th) -> {
            if (th != null)
                error(new IneundoError("Cannot retrieve player data on redis...", th));

            if (json != null) {
                var     retrieved = playersCollection.deserialize(Document.parse(json));
                Field[] fields    = retrieved.getClass().getDeclaredFields();
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        var newValue = field.get(retrieved);
                        if (newValue != null && !field.get(current).equals(newValue)) ReflectionUtil.setField(current, field.getName(), newValue);
                    } catch (IllegalAccessException e) {
                        error(e);
                    }
                }
            }
            return current;
        });
    }

    private void fetchPlayerData(UUID uuid) {
        var publisher = playersCollection.findOne(MongoConstants.PLAYER_ID_FILTER(uuid));
        publisher.thenApplyAsync(player -> {
            if (player == null) {
                player = new IslandsPlayer();
                player.firstConection(uuid);
            } else {
                player.welcomeBack();
            }
            return player;
        }).thenAcceptAsync(player -> getIneundo().addPlayer(player)).orTimeout(mongoTimeout, mongoTimeoutUnit).exceptionallyAsync(th -> {
            error(new IneundoError("MongoDB timeout when retrieving player data", th));
            return null;
        });
    }

    private CompletableFuture<Optional<IslandsPlayer>> getPlayerAsync(UUID uuid) {
        CompletableFuture<Optional<IslandsPlayer>> future = new CompletableFuture<>();

        var scheduledTask = getServer().getScheduler().buildTask(getIneundo(), () -> {
            var player = getPlayer(uuid);
            if (player.isPresent()) future.complete(player);
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
