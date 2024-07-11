package fr.islandswars.ineundo.manager.container;

import fr.islandswars.commons.service.docker.ContainerType;

/**
 * File <b>ContainerImage</b> located on fr.islandswars.ineundo.manager.container
 * ContainerImage is a part of ineundo.
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
public class ContainerImage {

    private static final String ISLANDS_IMAGE = "papermc-server:v1.0";
    private static final String HUB_IMAGE     = "papermc-server:v1.0";

    public static String getImage(ContainerType type) {
        var image = "";
        switch (type) {
            case ISLANDS -> image = ISLANDS_IMAGE;
            case HUB -> image = HUB_IMAGE;
        }
        return image;
    }
}
