package fr.islandswars.ineundo.log.internal;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import fr.islandswars.commons.log.Log;

import java.util.UUID;
import java.util.logging.Level;

/**
 * File <b>PlayerConnectionLog</b> located on fr.islandswars.ineundo.log.internal
 * PlayerConnectionLog is a part of ineundo.
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
 * Created the 26/06/2024 at 22:23
 * @since 0.1
 */
public class PlayerConnectionLog extends Log {

    private UUID            uuid;
    private String          name;
    @SerializedName("protocol_state")
    private String          protocolState;
    @SerializedName("protocol_version")
    private ProtocolVersion protocolVersion;
    private String          address;

    public PlayerConnectionLog(Level level, String msg) {
        super(level, msg, "proxy");//TODO proper access
    }

    @Override
    protected void checkValue() {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(protocolState);
        Preconditions.checkNotNull(protocolVersion);
        Preconditions.checkNotNull(address);
    }

    public PlayerConnectionLog withEvent(PreLoginEvent event) {
        this.protocolState = event.getConnection().getProtocolState().name();
        this.protocolVersion = event.getConnection().getProtocolVersion();
        this.address = event.getConnection().getRemoteAddress().toString();
        this.uuid = event.getUniqueId();
        this.name = event.getUsername();
        return this;
    }

    public PlayerConnectionLog withEvent(ServerPreConnectEvent event) {
        this.protocolState = event.getPlayer().getProtocolState().name();
        this.protocolVersion = event.getPlayer().getProtocolVersion();
        this.address = event.getPlayer().getRemoteAddress().toString();
        this.uuid = event.getPlayer().getUniqueId();
        this.name = event.getPlayer().getUsername();
        return this;
    }
}
