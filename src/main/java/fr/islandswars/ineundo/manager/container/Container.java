package fr.islandswars.ineundo.manager.container;

import java.util.UUID;

/**
 * File <b>Container</b> located on fr.islandswars.ineundo.manager.container
 * Container is a part of ineundo.
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
 * Created the 08/07/2024 at 13:11
 * @since 0.1
 */
public class Container {

    private final ContainerType type;
    private final UUID          containerID;

    public Container(ContainerType type) {
        this.type = type;
        this.containerID = UUID.randomUUID();
    }

    public String getImageID() {
        return type.getImageID();
    }

    public UUID getServerID() {
        //TODO check not already used
        return containerID;
    }

    public String getContainerName() {
        return type.name().toLowerCase() + "_" + containerID.toString();
    }

}
