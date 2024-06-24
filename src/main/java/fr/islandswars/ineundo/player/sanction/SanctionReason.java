package fr.islandswars.ineundo.player.sanction;

/**
 * File <b>SanctionReason</b> located on fr.islandswars.ineundo.player.sanction
 * SanctionReason is a part of ineundo.
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
 * Created the 24/06/2024 at 22:37
 * @since 0.1
 */
//TODO get kick message i18n
public enum SanctionReason {

    CHEAT("You cheat", 7),
    BEHAVIOR("Bad behavior", 365);

    private final String kickKey;
    private final int    days;

    SanctionReason(String kickKey, int days) {
        this.kickKey = kickKey;
        this.days = days;
    }

    public int getDays() {
        return days;
    }

    public String getKickKey() {
        return kickKey;
    }
}
