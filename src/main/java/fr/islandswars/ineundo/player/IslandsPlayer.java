package fr.islandswars.ineundo.player;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.velocitypowered.api.util.GameProfile;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.player.sanction.IslandsSanction;
import fr.islandswars.ineundo.utils.ProxyConstants;
import fr.islandswars.ineundo.utils.TimeUtils;

import java.time.Instant;
import java.util.*;

/**
 * File <b>IslandsPlayer</b> located on fr.islandswars.ineundo.player
 * IslandsPlayer is a part of ineundo.
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
 * Created the 23/06/2024 at 16:32
 * @since 0.1
 */
public class IslandsPlayer {

    @Expose
    private UUID                  uuid;
    @Expose
    private List<Rank>            ranks;
    @SerializedName("first_connection")
    @Expose
    private String                firstConnection;
    @SerializedName("last_connection")
    @Expose
    private String                lastConnection;
    @Expose
    private List<IslandsSanction> sanctions;
    @Expose
    private GameProfile.Property  profile;

    public IslandsPlayer() {
        this.ranks = new ArrayList<>();
        this.sanctions = new ArrayList<>();
    }

    public void firstConection(UUID uuid) {
        setUUID(uuid);
        this.firstConnection = TimeUtils.NOW();
        this.lastConnection = firstConnection;
        addRank(new Rank(IslandsRank.PLAYER, ProxyConstants.PROXY, TimeUtils.NOW()));
    }

    public void welcomeBack() {
        setLastConnection();
    }

    public IslandsRank getMainRank() {
        return IslandsRank.getHighest(ranks);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setLastConnection() {
        this.lastConnection = TimeUtils.NOW();
    }

    public void addSanction(IslandsSanction sanction) {
        sanctions.add(sanction);
        Ineundo.getInstance().getServer().getPlayer(getUUID()).ifPresent(p -> {
            p.disconnect(sanction.getKickMessage());
        });
    }

    public void addRank(Rank rank) {
        if (ranks.stream().noneMatch(r -> r.getRank().equals(rank.getRank())))
            ranks.add(rank);
        //TODO notify the player and update it !!
    }

    public Optional<IslandsSanction> isKick() {
        for (IslandsSanction sanction : sanctions) {
            var endDate = Instant.parse(sanction.getEnd());
            var now     = Instant.now();
            if (endDate.isAfter(now))
                return Optional.of(sanction);
        }
        return Optional.empty();
    }

    public GameProfile.Property getProfile() {
        return profile;
    }

    public void setProfile(GameProfile.Property profile) {
        this.profile = profile;
    }
}

