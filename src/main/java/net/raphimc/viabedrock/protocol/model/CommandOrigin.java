/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import java.util.Objects;
import java.util.UUID;

public class CommandOrigin {

    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_MINECART_BLOCK = 2;
    public static final int TYPE_DEV_CONSOLE = 3;
    public static final int TYPE_TEST = 4;
    public static final int TYPE_AUTOMATION_PLAYER = 5;
    public static final int TYPE_CLIENT_AUTOMATION = 6;
    public static final int TYPE_DEDICATED_SERVER = 7;
    public static final int TYPE_ENTITY = 8;
    public static final int TYPE_VIRTUAL = 9;
    public static final int TYPE_GAME_ARGUMENT = 10;
    public static final int TYPE_ENTITY_SERVER = 11;
    public static final int TYPE_PRECOMPILED = 12;
    public static final int TYPE_GAME_DIRECTOR_ENTITY_SERVER = 13;
    public static final int TYPE_SCRIPT = 14;
    public static final int TYPE_EXECUTE_CONTEXT = 15;

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
