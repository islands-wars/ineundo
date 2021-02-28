package fr.islandswars.ineundo.player.i18n;

import fr.islandswars.ineundo.player.Locale;

/**
 * File <b>Translatable</b> located on fr.islandswars.ineundo.player.i18n
 * Translatable is a part of ineundo.
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
 * Created the 28/02/2021 at 15:43
 * @since 0.1
 */
public class Translatable {

	private static final Locale             DEFAULT = Locale.FRENCH;
	private final        TranslatableLoader loader;

	public Translatable() {
		this.loader = new TranslatableLoader();
	}

	public String format(String key, Object... parameters) {
		return String.format(loader.values.get(DEFAULT).getOrDefault(key, key), parameters);
	}

	public String format(Locale locale, String key, Object... parameters) {
		return String.format(loader.values.get(locale).getOrDefault(key, key), parameters);
	}

	public TranslatableLoader getLoader() {
		return loader;
	}
}
