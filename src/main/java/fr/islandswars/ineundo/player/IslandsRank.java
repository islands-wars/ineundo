package fr.islandswars.ineundo.player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * File <b>IslandsRank</b> located on fr.islandswars.ineundo.player
 * IslandsRank is a part of ineundo.
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
 * Created the 24/06/2024 at 16:07
 * @since 0.1
 */
public enum IslandsRank {

    ADMIn(1),
    PLAYER(5);

    private final int rankLevel;

    IslandsRank(int rankLevel) {
        this.rankLevel = rankLevel;
    }

    public static IslandsRank getHighest(List<String> ranks) {
        return Arrays.stream(IslandsRank.values()).filter(r -> ranks.contains(r.name())).min(Comparator.comparingInt(IslandsRank::getRankLevel)).orElse(PLAYER);
    }

    public int getRankLevel() {
        return rankLevel;
    }
}
