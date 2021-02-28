package fr.islandswars.ineundo.player;

import com.google.gson.annotations.SerializedName;

/**
 * File <b>Sanction</b> located on fr.islandswars.ineundo.player
 * Sanction is a part of ineundo.
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
 * Created the 28/02/2021 at 16:55
 * @since 0.1
 */
public class Sanction {

	@SerializedName("author")
	private final String         authorName;
	@SerializedName("time")
	private final long           sanctionTime;
	private final SanctionReason reason;
	private final SanctionType   type;
	@SerializedName("end")
	private       long           sanctionEndTime;
	private       Pardon         pardon;

	public Sanction(String authorName, long sanctionTime, SanctionReason reason, SanctionType type) {
		this.authorName = authorName;
		this.sanctionTime = sanctionTime;
		this.reason = reason;
		this.type = type;
	}

	public boolean isPardon() {
		return pardon != null;
	}

	public boolean canJoin() {
		var current = System.currentTimeMillis();
		if (isPardon())
			return true;
		return current > sanctionEndTime;
	}

	public void setSanctionEndTime(long sanctionEndTime) {
		this.sanctionEndTime = sanctionEndTime;
	}

	public long getSanctionEndTime() {
		return sanctionEndTime;
	}

	public SanctionType getType() {
		return type;
	}

	public long getSanctionTime() {
		return sanctionTime;
	}

	public SanctionReason getReason() {
		return reason;
	}

	public String getAuthor() {
		return authorName;
	}
}
