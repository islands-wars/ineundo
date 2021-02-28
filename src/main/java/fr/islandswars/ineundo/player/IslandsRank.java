package fr.islandswars.ineundo.player;

/**
 * File <b>IslandsRank</b> located on fr.islandswars.ineundo.player
 * IslandsRank is a part of ineundo.
 * <p>
 * Copyright (c) 2017 - 2021 Islands Wars.
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
 * @author Valentin Burgaud (Xharos), {@literal <xharos@islandswars.fr>}
 * Created the 28/02/2021 at 16:49
 * @since 0.1
 */
public enum IslandsRank {

	PLAYER(1),
	CUSTOM(2),
	STAFF(10),
	ADMIN(11);

	int level;

	private IslandsRank(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

}
