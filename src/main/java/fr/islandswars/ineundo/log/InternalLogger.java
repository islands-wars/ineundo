package fr.islandswars.ineundo.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.islandswars.commons.log.IslandsLogger;
import fr.islandswars.commons.log.LevelTypeAdapter;
import fr.islandswars.commons.log.Log;
import fr.islandswars.commons.log.StackTraceElementTypeAdapter;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.URISyntaxException;
import java.util.logging.Level;

/**
 * File <b>InternalLogger</b> located on fr.islandswars.ineundo.log
 * InternalLogger is a part of ineundo.
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
 * Created the 25/06/2024 at 19:21
 * @since 0.1
 */
public class InternalLogger extends IslandsLogger {

    private final Gson gson;

    public InternalLogger(String containerName) {
        super(containerName);
        this.gson = new GsonBuilder().registerTypeAdapter(Level.class, new LevelTypeAdapter()).registerTypeAdapter(StackTraceElement.class, new StackTraceElementTypeAdapter()).create();
        //overrideDefault(); //TODO update
    }

    private void overrideDefault() {
        try {
            Configurator.reconfigure(InternalLogger.class.getClassLoader().getResource("log4j2-islands.xml").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sysout(Log log) {
        System.out.println(gson.toJson(log));
    }
}