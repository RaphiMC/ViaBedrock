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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.UUID;

public class PlayerEntity extends Entity {

    public PlayerEntity(final UserConnection user, final long uniqueId, final long runtimeId, final int javaId, final UUID javaUuid) {
        super(user, uniqueId, runtimeId, javaId, javaUuid, Entity1_19_4Types.PLAYER);
    }

    public void createTeam() throws Exception {
        final PacketWrapper teams = PacketWrapper.create(ClientboundPackets1_19_4.TEAMS, this.user);
        teams.write(Type.STRING, "vb_" + this.javaId); // team name
        teams.write(Type.BYTE, (byte) 0); // mode | 0 = ADD
        teams.write(Type.COMPONENT, JsonUtil.textToComponent("vb_" + this.javaId)); // display name
        teams.write(Type.BYTE, (byte) 3); // flags
        teams.write(Type.STRING, "always"); // name tag visibility
        teams.write(Type.STRING, "never"); // collision rule
        teams.write(Type.VAR_INT, 21); // color | 21 = RESET
        teams.write(Type.COMPONENT, JsonUtil.textToComponent("")); // prefix
        teams.write(Type.COMPONENT, JsonUtil.textToComponent("")); // suffix
        teams.write(Type.STRING_ARRAY, new String[]{StringUtil.encodeUUID(this.javaUuid)}); // players
        teams.send(BedrockProtocol.class);
    }

    public void updateName(final String name) throws Exception {
        this.setName(name);

        final PacketWrapper teams = PacketWrapper.create(ClientboundPackets1_19_4.TEAMS, this.user);
        teams.write(Type.STRING, "vb_" + this.javaId); // team name
        teams.write(Type.BYTE, (byte) 2); // mode | 2 = UPDATE
        teams.write(Type.COMPONENT, JsonUtil.textToComponent("vb_" + this.javaId)); // display name
        teams.write(Type.BYTE, (byte) 3); // flags
        teams.write(Type.STRING, "always"); // name tag visibility
        teams.write(Type.STRING, "never"); // collision rule
        teams.write(Type.VAR_INT, 21); // color | 21 = RESET
        teams.write(Type.COMPONENT, JsonUtil.textToComponent(name)); // prefix
        teams.write(Type.COMPONENT, JsonUtil.textToComponent("")); // suffix
        teams.send(BedrockProtocol.class);
    }

    public void deleteTeam() throws Exception {
        final PacketWrapper teams = PacketWrapper.create(ClientboundPackets1_19_4.TEAMS, this.user);
        teams.write(Type.STRING, "vb_" + this.javaId); // team name
        teams.write(Type.BYTE, (byte) 1); // mode | 1 = REMOVE
        teams.send(BedrockProtocol.class);
    }

    @Override
    public float eyeOffset() {
        return 1.62F;
    }

}
