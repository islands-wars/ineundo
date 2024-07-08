package fr.islandswars.ineundo.manager.container;

/**
 * File <b>ContainerType</b> located on fr.islandswars.ineundo.manager.container
 * ContainerType is a part of ineundo.
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
 * Created the 08/07/2024 at 13:15
 * @since 0.1
 */
public enum ContainerType {

    ISLANDS("papermc-server:v1.0"),
    HUB("papermc-server");

    private final String imageID;

    ContainerType(String imageID) {
        this.imageID = imageID;
    }

    public String getImageID() {
        return imageID;
    }
}
