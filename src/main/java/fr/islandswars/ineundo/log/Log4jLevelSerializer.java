package fr.islandswars.ineundo.log;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Type;

/**
 * File <b>Log4jLevelSerializer</b> located on fr.islandswars.core.internal.log
 * Log4jLevelSerializer is a part of islands.
 * <p>
 * Copyright (c) 2017 - 2024 Islands Wars.
 * <p>
 * islands is free software: you can redistribute it and/or modify
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
 * Created the 30/03/2024 at 17:51
 * @since 0.1
 */
public class Log4jLevelSerializer implements JsonSerializer<Level> {

    @Override
    public JsonElement serialize(Level level, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(level.name());
    }
}
