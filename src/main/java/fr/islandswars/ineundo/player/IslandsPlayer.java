package fr.islandswars.ineundo.player;

import com.velocitypowered.api.proxy.Player;
import java.util.*;

/**
 * File <b>IslandsPlayer</b> located on fr.islandswars.ineundo.player
 * IslandsPlayer is a part of ineundo.
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
 * Created the 24/02/2021 at 09:05
 * @since 0.1
 */
public class IslandsPlayer {

	private Locale           locale;
	private UUID             id;
	private String           name;
	private long             firstCo;
	private Set<IslandsRank> ranks;
	private List<Sanction>   sanctions;

	public IslandsPlayer(Player player) {
		this(player.getUniqueId(), player.getUsername());
	}

	public IslandsPlayer(UUID id, String userName) {
		this.name = userName;
		this.id = id;
		this.locale = Locale.FRENCH;
		this.ranks = new HashSet<>();
		this.sanctions = new ArrayList<>();
		ranks.add(IslandsRank.PLAYER);
	}

	public void addSanction(Sanction sanction) {
		sanctions.add(sanction);
	}

	public String getName() {
		return name;
	}

	public UUID getId() {
		return id;
	}

	public void depucelage() {
		this.firstCo = System.currentTimeMillis();
	}

	public boolean isStaff() {
		for (IslandsRank rank : ranks) {
			if (rank.getLevel() >= 10)
				return true;
		}
		return false;
	}

	public Locale getLocale() {
		return locale;
	}

	public void addRank(IslandsRank rank) {
		ranks.add(rank);
	}

	public Optional<Sanction> isBanned() {
		return sanctions.stream().filter(sanction -> !sanction.canJoin()).findFirst();
	}
}
