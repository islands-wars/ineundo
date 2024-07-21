package fr.islandswars.ineundo.utils;

import java.util.UUID;

/**
 * File <b>RedisConstants</b> located on fr.islandswars.ineundo.utils
 * RedisConstants is a part of ineundo.
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
 * Created the 25/06/2024 at 23:38
 * @since 0.1
 */
public class RedisConstants {

    public static final  String PROXY          = "proxies";
    public static final  String SERVER_LISTS   = "servers";
    private static final String PLAYER         = "player";
    private static final String S_TYPE         = "type";
    private static final String S_NAME         = "name";
    private static final String S_PLAYER_COUNT = "pcount";
    private static final String S_STATUS       = "status";
    public static final  String PLAYER_COUNT   = "player:count";

    public static String PLAYER_KEY(UUID uuid) {
        return uuid.toString() + ":" + PLAYER;
    }

    public static String SERVER_TYPE(UUID uuid) {
        return uuid.toString() + ":" + S_TYPE;
    }

    public static String SERVER_NAME(UUID uuid) {
        return uuid.toString() + ":" + S_NAME;
    }

    public static String SERVER_PLAYER_COUNT(UUID uuid) {
        return uuid.toString() + ":" + S_PLAYER_COUNT;
    }

    public static String SERVER_STATUS(UUID uuid) {
        return uuid.toString() + ":" + S_STATUS;
    }
}
