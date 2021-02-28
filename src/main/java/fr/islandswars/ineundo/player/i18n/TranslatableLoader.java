package fr.islandswars.ineundo.player.i18n;

import fr.islandswars.ineundo.Ineundo;
import fr.islandswars.ineundo.player.Locale;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarFile;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;

/**
 * File <b>TranslatableLoader</b> located on fr.islandswars.ineundo.player.i18n
 * TranslatableLoader is a part of ineundo.
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
public class TranslatableLoader {

	ConcurrentMap<Locale, ConcurrentMap<String, String>> values;

	TranslatableLoader() {
		this.values = new ConcurrentHashMap<>();
		for (var locale : Locale.values())
			values.put(locale, new ConcurrentHashMap<>());
	}

	@SuppressWarnings("unchecked")
	public void registerCustomProperties(Ineundo ineundo, Path pluginFolder) {
		var i18nFolder = new File(pluginFolder.toFile(), "i18n/");

		if (!i18nFolder.exists())
			i18nFolder.mkdirs();

		try {
			URI     pluginURI = ineundo.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
			JarFile jarPlugin = new JarFile(pluginURI.getPath());
			for (var jarEntry : Collections.list(jarPlugin.entries())) {
				if (!jarEntry.isDirectory() && jarEntry.getName().startsWith("i18n/")) {
					InputStream      in  = Ineundo.class.getResourceAsStream("/" + jarEntry.getName());
					FileOutputStream out = new FileOutputStream(i18nFolder + "/" + jarEntry.getName().split("/")[1]);
					while (in.available() > 0) {
						out.write(in.read());
					}
					out.close();
					in.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Arrays.stream(i18nFolder.listFiles()).filter(file -> file.getName().endsWith(".lang")).forEach(this::loadFile);
	}

	public void registerDynamicProperty(Locale locale, String key, String value) {
		values.computeIfPresent(locale, (k, v) -> {
			v.putIfAbsent(key, value);
			return v;
		});
	}

	private Locale getLocale(File lang) {
		var name = lang.getName();
		for (var locale : Locale.values()) {
			if (name.contains(locale.getI18nName()))
				return locale;
		}
		return null;
	}

	private void loadFile(File langFile) {
		var loc = getLocale(langFile);
		if (loc != null) {
			var               properties = new Properties();
			FileInputStream   stream     = null;
			InputStreamReader reader     = null;
			try {
				stream = new FileInputStream(langFile);
				reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				properties.load(reader);
				properties.forEach((langKey, langValue) -> values.computeIfPresent(loc, (locale, map) -> {
					map.putIfAbsent(langKey.toString(), langValue.toString());
					return map;
				}));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (stream != null)
						stream.close();
					if (reader != null)
						reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
