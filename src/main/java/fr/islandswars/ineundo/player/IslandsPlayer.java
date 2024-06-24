package fr.islandswars.ineundo.player;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
    private UUID         uuid;
    @Expose
    private List<String> ranks;
    @SerializedName("first_connection")
    @Expose
    private String       firstConnection;
    @SerializedName("last_connection")
    @Expose
    private String       lastConnection;

    public IslandsPlayer() {
        this.ranks = List.of(IslandsRank.PLAYER.toString());
        this.firstConnection = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        this.lastConnection = firstConnection;
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

    public void setRanks(List<String> ranks) {
        this.ranks = ranks;
    }

    public void setLastConnection() {
        this.lastConnection = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    public String getFirstConnection() {
        return firstConnection;
    }

    @Override
    public String toString() {
        return "IslandsPlayer:" + uuid + ":" + ranks;
    }
}

