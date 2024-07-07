package fr.islandswars.ineundo.listener;

import com.mongodb.client.result.UpdateResult;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import fr.islandswars.commons.service.collection.Collection;
import fr.islandswars.commons.service.mongodb.MongoDBConnection;
import fr.islandswars.commons.service.mongodb.OperationSubscriber;
import fr.islandswars.commons.service.redis.RedisConnection;
import fr.islandswars.commons.utils.ReflectionUtil;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.log.internal.PlayerConnectionLog;
import fr.islandswars.ineundo.player.ProxyPlayer;
import fr.islandswars.ineundo.utils.MongoConstants;
import fr.islandswars.ineundo.utils.ProxyConstants;
import fr.islandswars.ineundo.utils.RedisConstants;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Level;
import org.bson.Document;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

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

    private final long                                    mongoTimeout     = 1L;
    private final TimeUnit                                mongoTimeoutUnit = TimeUnit.SECONDS;
    private final List<OperationSubscriber<UpdateResult>> pendingResults;
    private final Collection<ProxyPlayer>                 playersCollection;
    private final RedisConnection                         redis;

    public PlayerDataListener(Ineundo ineundo, MongoDBConnection mongo, RedisConnection redis) {
        super(ineundo);
        this.redis = redis;
        this.pendingResults = new CopyOnWriteArrayList<>();
        this.playersCollection = mongo.getCollection(MongoConstants.PLAYER_COLLECTION, ProxyPlayer.class);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(PreLoginEvent event) {
        var uuid = event.getUniqueId();
        getIneundo().getPlayer(uuid).ifPresentOrElse(p -> {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.translatable("event.join.data.error")));
            new PlayerConnectionLog(Level.ERROR, "Player not saved in database").withEvent(event).log();
        }, () -> fetchPlayerData(uuid, event));
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onServerPreconnectEvent(ServerPreConnectEvent event) {
        if (event.getPreviousServer() == null) {
            return EventTask.async(() -> synchroniseData(event));
        } else
            return null;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent event) {
        var uuid      = event.getPlayer().getUniqueId();
        var optPlayer = getPlayer(uuid);
        optPlayer.ifPresent(this::savePlayerData);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        while (!pendingResults.isEmpty()) {
        }
    }

    private void savePlayerData(ProxyPlayer player) {
        updateFromRedis(player).whenCompleteAsync((p, th) -> {
            if (th != null)
                error(new IneundoError("Error when retrieving player data from Redis", th));

            var subscriber = playersCollection.replace(p, MongoConstants.PLAYER_ID_FILTER(p.getUUID()));
            pendingResults.add(subscriber);

            CompletableFuture<UpdateResult> result = new CompletableFuture<>();
            result.completeAsync(subscriber::first).orTimeout(mongoTimeout, mongoTimeoutUnit);
            result.whenCompleteAsync((re, thr) -> {
                if (thr != null) {
                    error(new IneundoError("Error when saving player data in MongoDB.", thr));
                    getIneundo().getInfraLogger().log(Level.ERROR, playersCollection.serialize(p).toJson()); //manual save in case of problem
                }
                getIneundo().removePlayer(player);
                pendingResults.remove(subscriber);
            });
        });
    }

    private void synchroniseData(ServerPreConnectEvent event) {
        getPlayerAsync(event.getPlayer().getUniqueId()).whenCompleteAsync((optPlayer, th) -> {
            if (th != null || optPlayer.isEmpty()) {
                //error(new IneundoError("Player " + event.getPlayer().getUsername() + " cannot be retrieved in time", th));
                event.getPlayer().disconnect(Component.translatable("event.join.data.error"));
                new PlayerConnectionLog(Level.ERROR, "Cannot retrieve data from mongodb in time").withEvent(event).log();
            } else {
                var player = optPlayer.get();
                injectGameProfile(player, event.getPlayer());
                var sanction = player.isKick();
                sanction.ifPresent(s -> {
                    event.getPlayer().disconnect(Component.translatable(s.getReason().getKickKey(), Component.text(s.getAuthorName()), Component.text(s.getEnd())));
                    new PlayerConnectionLog(Level.INFO, "Kicked player attempt to login").withEvent(event).log();
                });
                if (getIneundo().getSTAFF_ONLY().get() && !player.getMainRank().isStaff()) {
                    event.getPlayer().disconnect(Component.translatable("event.join.staff"));
                    new PlayerConnectionLog(Level.INFO, "Connection attempt when the server is in maintenance").withEvent(event).log();
                } else {
                    //TODO server offline
                    redis.getConnection().set(RedisConstants.PLAYER_KEY(player.getUUID()), playersCollection.serialize(player).toJson()).whenCompleteAsync((re, thr) -> {
                        if (thr != null) {
                            event.getPlayer().disconnect(Component.translatable("event.join.data.error"));
                            new PlayerConnectionLog(Level.ERROR, "Cannot save data in redis").withEvent(event).log();
                        } else
                            new PlayerConnectionLog(Level.INFO, "Successful connection").withEvent(event).log();
                    });
                }
            }
        });
    }

    private CompletionStage<ProxyPlayer> updateFromRedis(ProxyPlayer current) {
        return redis.getConnection().get(RedisConstants.PLAYER_KEY(current.getUUID())).thenApply((json) -> {
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

    private void fetchPlayerData(UUID uuid, PreLoginEvent event) {
        var publisher = playersCollection.findOne(MongoConstants.PLAYER_ID_FILTER(uuid));
        publisher.thenApplyAsync(player -> {
            if (!event.getConnection().getProtocolVersion().isSupported()) {
                new PlayerConnectionLog(Level.WARN, "Minecraft version not supported!").withEvent(event).log();
                throw new UnsupportedOperationException("Outdated client version");
            }
            PlayerConnectionLog log;
            if (player == null) {
                player = new ProxyPlayer();
                player.firstConnection(uuid, ProxyConstants.PROXY);
                log = new PlayerConnectionLog(Level.INFO, "First login attempt to join the server");
            } else {
                player.welcomeBack();
                log = new PlayerConnectionLog(Level.INFO, "Login attempt to join the server");
            }
            log.withEvent(event).log();
            return player;
        }).thenAcceptAsync(player -> getIneundo().addPlayer(player)).orTimeout(mongoTimeout, mongoTimeoutUnit).exceptionallyAsync(th -> {
            if (!(th instanceof UnsupportedOperationException))//check if mongo can throw this error
                error(new IneundoError(th));
            return null;
        });
    }

    private CompletableFuture<Optional<ProxyPlayer>> getPlayerAsync(UUID uuid) {
        CompletableFuture<Optional<ProxyPlayer>> future = new CompletableFuture<>();

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

    private void injectGameProfile(ProxyPlayer isPlayer, Player player) {
        var profileProperty = player.getGameProfile().getProperties().stream().filter(prop -> prop.getName().equals("textures")).findFirst();
        profileProperty.ifPresent(prop ->{
            if (isPlayer.getProfile() == null || !isPlayer.getProfile().equals(prop))
                isPlayer.setProfile(prop);
        });

    }
}
