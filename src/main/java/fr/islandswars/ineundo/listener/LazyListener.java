package fr.islandswars.ineundo.listener;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.player.ProxyPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * File <b>LazyListener</b> located on fr.islandswars.ineundo.listener
 * LazyListener is a part of ineundo.
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
 * Created the 24/06/2024 at 16:16
 * @since 0.1
 */
public abstract class LazyListener {

    private final ProxyServer server;
    private final Ineundo     ineundo;

    public LazyListener(Ineundo ineundo) {
        this.ineundo = ineundo;
        this.server = ineundo.getServer();
        server.getEventManager().register(ineundo, this);
    }

    public Ineundo getIneundo() {
        return ineundo;
    }

    public ProxyServer getServer() {
        return server;
    }

    public void log(Object msg) {
        IslandsLogger.getLogger().logInfo(msg);
    }

    public void error(Exception e) {
        IslandsLogger.getLogger().logError(e);
    }

    public Optional<ProxyPlayer> getPlayer(UUID uuid) {
        return getIneundo().getPlayer(uuid);
    }

}
