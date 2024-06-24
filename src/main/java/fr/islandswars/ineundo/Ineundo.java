package fr.islandswars.ineundo;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.islandswars.commons.service.mongodb.MongoDBConnection;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * File <b>Ineundo</b> located on fr.islandswars.ineundo
 * Ineundo is a part of ineundo.
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
 * Created the 18/06/2024 at 00:10
 * @since 0.1
 */
@Plugin(
        id = "ineundo",
        name = "Ineundo",
        version = "0.1",
        authors = "Xharos"
)
public class Ineundo {

    private Logger      logger;
    private ProxyServer server;

    @Inject
    public Ineundo(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.server = server;
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        logger.info("hello world");
        MongoDBConnection conn = new MongoDBConnection();

        try {
            conn.load();
            conn.connect();
            logger.info(conn.getConnection().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
