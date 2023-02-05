/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viabedrock.protocol.model;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandOriginTypes;

import java.util.Objects;
import java.util.UUID;

public class CommandOrigin {

    /**
     * Possible types: {@link CommandOriginTypes}
     */
    private final int type;
    private final UUID uuid;
    private final String requestId;
    private final long event;

    public CommandOrigin(final int type, final UUID uuid, final String requestId) {
        this(type, uuid, requestId, -1);
    }

    public CommandOrigin(final int type, final UUID uuid, final String requestId, final long event) {
        this.type = type;
        this.uuid = uuid;
        this.requestId = requestId;
        this.event = event;
    }

    public CommandOrigin(final CommandOrigin type) {
        this.type = type.type;
        this.uuid = type.uuid;
        this.requestId = type.requestId;
        this.event = type.event;
    }

    public int type() {
        return this.type;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public String requestId() {
        return this.requestId;
    }

    public long event() {
        return this.event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandOrigin that = (CommandOrigin) o;
        return type == that.type && event == that.event && Objects.equals(uuid, that.uuid) && Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, uuid, requestId, event);
    }

    @Override
    public String toString() {
        return "CommandOrigin{" +
                "type=" + type +
                ", uuid=" + uuid +
                ", requestId='" + requestId + '\'' +
                ", event=" + event +
                '}';
    }

}
