package fr.islandswars.ineundo.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.islandswars.commons.utils.ReflectionUtil;
import fr.islandswars.ineundo.lang.IneundoError;
import fr.islandswars.ineundo.log.internal.DefaultLog;
import fr.islandswars.ineundo.log.internal.ErrorLog;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.URISyntaxException;

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
public class InternalLogger {

    private final Logger  rootLogger;
    private final Gson    gson;
    private final boolean debug;

    public InternalLogger() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Level.class, new Log4jLevelSerializer())
                .registerTypeAdapter(StackTraceElement.class, new StackTraceElementTypeAdapter())
                .create();
        this.debug = Boolean.parseBoolean(System.getenv("DEBUG"));
        this.rootLogger = (Logger) LogManager.getRootLogger();
        //overrideDefault(); //TODO update
    }

    private void overrideDefault() {
        try {
            Configurator.reconfigure(InternalLogger.class.getClassLoader().getResource("log4j2-islands.xml").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void logInfo(String message) {
        log(Level.INFO, message);
    }

    public void log(Level level, String msg) {
        if (level.equals(Level.ERROR))
            logError(new IneundoError(msg));
        else
            new DefaultLog(level, msg).log();
    }

    public void logDebug(String message) {
        log(Level.DEBUG, message);
    }

    public <T extends Log> T createCustomLog(Class<T> clazz, Level level, String message) {
        return ReflectionUtil.getConstructorAccessor(clazz, Level.class, String.class).newInstance(level, message);
    }

    public void logError(Exception e) {
        new ErrorLog(Level.ERROR, e.getMessage() == null ? "Error" : e.getMessage()).supplyStacktrace(e.fillInStackTrace()).log();
    }

    protected void sysout(Log object) {
        System.out.print(object.msg);
        if (object.getLevel() == Level.DEBUG) {
            if (debug)
                rootLogger.log(object.getLevel(), gson.toJson(object));
        } else
            rootLogger.log(object.getLevel(), gson.toJson(object));
    }
}