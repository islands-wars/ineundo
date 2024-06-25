package fr.islandswars.ineundo.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * File <b>TimeUtils</b> located on fr.islandswars.ineundo.utils
 * TimeUtils is a part of ineundo.
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
 * Created the 25/06/2024 at 22:17
 * @since 0.1
 */
public class TimeUtils {

    public static String NOW() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    public static Instant FROM_ISO_STRING(String date) {
        return Instant.parse(date);
    }
}
