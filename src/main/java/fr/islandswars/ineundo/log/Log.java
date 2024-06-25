package fr.islandswars.ineundo.log;

import com.google.gson.annotations.SerializedName;
import fr.islandswars.ineundo.Ineundo;
import org.apache.logging.log4j.Level;

import java.time.Instant;
import java.time.format.DateTimeFormatter;


/**
 * File <b>Log</b> located on fr.islandswars.api.log
 * Log is a part of islands.
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
 * Created the 26/03/2024 at 19:30
 * @since 0.1
 */
public abstract class Log {

    protected final Level  level;
    @SerializedName("message")
    protected final String msg;
    protected       String date;

    protected Log(Level level, String msg) {
        this.level = level;
        this.msg = msg;
    }

    public void log() {
        checkValue();
        this.date = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        Ineundo.getInstance().getInfraLogger().sysout(this);
    }

    protected abstract void checkValue();

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return msg;
    }
}
