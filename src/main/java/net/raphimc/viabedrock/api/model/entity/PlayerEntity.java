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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerTeamAction;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;

import java.util.UUID;

public class PlayerEntity extends LivingEntity {

    protected PlayerAbilities abilities;

    public PlayerEntity(final UserConnection user, final long runtimeId, final int javaId, final UUID javaUuid, final PlayerAbilities abilities) {
        super(user, abilities.uniqueEntityId(), runtimeId, javaId, javaUuid, EntityTypes1_20_5.PLAYER);

        this.abilities = abilities;
    }

    public void createTeam() {
        final PacketWrapper setPlayerTeam = PacketWrapper.create(ClientboundPackets1_21.SET_PLAYER_TEAM, this.user);
        setPlayerTeam.write(Types.STRING, "vb_" + this.javaId); // team name
        setPlayerTeam.write(Types.BYTE, (byte) PlayerTeamAction.ADD.ordinal()); // mode
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("vb_" + this.javaId)); // display name
        setPlayerTeam.write(Types.BYTE, (byte) 3); // flags
        setPlayerTeam.write(Types.STRING, "always"); // name tag visibility
        setPlayerTeam.write(Types.STRING, "never"); // collision rule
        setPlayerTeam.write(Types.VAR_INT, TextFormatting.RESET.getOrdinal()); // color
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("")); // prefix
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("")); // suffix
        setPlayerTeam.write(Types.STRING_ARRAY, new String[]{StringUtil.encodeUUID(this.javaUuid)}); // players
        setPlayerTeam.send(BedrockProtocol.class);
    }

    public void updateName(final String name) {
        this.setName(name);

        final PacketWrapper setPlayerTeam = PacketWrapper.create(ClientboundPackets1_21.SET_PLAYER_TEAM, this.user);
        setPlayerTeam.write(Types.STRING, "vb_" + this.javaId); // team name
        setPlayerTeam.write(Types.BYTE, (byte) PlayerTeamAction.CHANGE.ordinal()); // mode
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("vb_" + this.javaId)); // display name
        setPlayerTeam.write(Types.BYTE, (byte) 3); // flags
        setPlayerTeam.write(Types.STRING, "always"); // name tag visibility
        setPlayerTeam.write(Types.STRING, "never"); // collision rule
        setPlayerTeam.write(Types.VAR_INT, TextFormatting.RESET.getOrdinal()); // color
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt(name)); // prefix
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("")); // suffix
        setPlayerTeam.send(BedrockProtocol.class);
    }

    public void deleteTeam() {
        final PacketWrapper setPlayerTeam = PacketWrapper.create(ClientboundPackets1_21.SET_PLAYER_TEAM, this.user);
        setPlayerTeam.write(Types.STRING, "vb_" + this.javaId); // team name
        setPlayerTeam.write(Types.BYTE, (byte) PlayerTeamAction.REMOVE.ordinal()); // mode
        setPlayerTeam.send(BedrockProtocol.class);
    }

    @Override
    public float eyeOffset() {
        return 1.62F;
    }

    public PlayerAbilities abilities() {
        return this.abilities;
    }

    public void setAbilities(final PlayerAbilities abilities) {
        this.abilities = abilities;
    }

}
