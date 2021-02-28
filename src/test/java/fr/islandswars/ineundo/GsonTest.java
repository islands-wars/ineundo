package fr.islandswars.ineundo;

import com.google.gson.Gson;
import fr.islandswars.ineundo.player.IslandsPlayer;
import fr.islandswars.ineundo.player.IslandsRank;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * File <b>GsonTest</b> located on fr.islandswars.ineundo
 * GsonTest is a part of ineundo.
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
 * Created the 28/02/2021 at 16:57
 * @since 0.1
 */
public class GsonTest {

	private final Gson gson = new Gson();
	private final String json = "{\"locale\":\"FRENCH\",\"id\":\"8b6de98c-f877-4bb1-8753-bbbef28acfe5\",\"name\":\"Xharos\",\"firstCo\":0,\"ranks\":[\"PLAYER\",\"ADMIN\"],\"sanctions\":[]}";

	@Test
	void gsonPlayerSerialization() {
		var p    = new IslandsPlayer(UUID.fromString("8b6de98c-f877-4bb1-8753-bbbef28acfe5"), "Xharos");
		p.addRank(IslandsRank.ADMIN);
		Assertions.assertEquals(gson.toJson(p).length(), json.length());
	}

	@Test
	void gsonPlayerDeserialization() {
		var player = gson.fromJson(json, IslandsPlayer.class);
		Assertions.assertTrue(player.isStaff());
	}

}
