package fr.islandswars.ineundo.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.utils.RedisConstants;
import io.lettuce.core.api.async.RedisAsyncCommands;
import net.kyori.adventure.text.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * File <b>ServerPingListener</b> located on fr.islandswars.ineundo.listener
 * ServerPingListener is a part of ineundo.
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
 * Created the 08/07/2024 at 19:00
 * @since 0.1
 */
public class ServerPingListener extends LazyListener {

    private final ServerPing.Version version;
    private final Favicon            favicon;
    private final Component          description;
    private       int                onlinePlayer;

    public ServerPingListener(Ineundo ineundo, RedisAsyncCommands<String, String> redis) {
        super(ineundo);
        var protocol = ProtocolVersion.MINECRAFT_1_21.getProtocol();
        var name     = ProtocolVersion.MINECRAFT_1_21.getVersionIntroducedIn();
        this.version = new ServerPing.Version(protocol, name);
        this.favicon = Favicon.create(loadFavicon());
        this.description = Component.text("Islands Wars");
        this.onlinePlayer = 0;

        ineundo.getServer().getScheduler().buildTask(ineundo, () -> {
            redis.get(RedisConstants.PLAYER_COUNT).whenCompleteAsync((re, th) -> {
                if (th != null)
                    error(new IneundoError("Canno't retrieve player count for list ping", th));
                try {
                    onlinePlayer = Integer.parseInt(re);
                } catch (Exception e) {
                    //backup solution
                    onlinePlayer = getServer().getPlayerCount();
                }
            });
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onServerPing(ProxyPingEvent event) {
        event.setPing(buildServerPing());
    }

    private ServerPing buildServerPing() {
        var players = new ServerPing.Players(onlinePlayer, 1000, Collections.emptyList());
        return new ServerPing(version, players, description, favicon);
    }

    private BufferedImage loadFavicon() {
        try (InputStream is = getClass().getResourceAsStream("/favicon.png")) {
            if (is == null) {
                error(new IOException("Favicon file not found: /favicon.png"));
                return null;
            } else
                return ImageIO.read(is);
        } catch (IOException e) {
            error(e);
            return null;
        }
    }
}
