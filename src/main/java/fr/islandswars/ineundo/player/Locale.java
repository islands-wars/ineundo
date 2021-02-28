package fr.islandswars.ineundo.player;

import fr.islandswars.ineundo.Ineundo;

/**
 * File <b>Locale</b> located on fr.islandswars.ineundo.player
 * Locale is a part of ineundo.
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
 * Created the 28/02/2021 at 15:41
 * @since 0.1
 */
public enum Locale {

	FRENCH("fr-FR"),
	ENGLISH("en-GB");

	private final String i18nName;

	Locale(String i18nName) {
		this.i18nName = i18nName;
	}

	/**
	 * Get a String according to this language and format with the given parameters
	 *
	 * @param key        the property key
	 * @param parameters the properties label to format with
	 * @return a {@link String#format(String, Object...)}, or else the key
	 */
	public String format(String key, Object... parameters) {
		return Ineundo.getInstance().getTranslatable().format(this, key, parameters);
	}

	/**
	 * @return this lang id
	 */
	public String getI18nName() {
		return i18nName;
	}
}
