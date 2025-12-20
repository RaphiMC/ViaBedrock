/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.raphimc.viabedrock.api.util.StringUtil;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerTeamAction;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.TeamCollisionRule;
import net.raphimc.viabedrock.protocol.data.enums.java.generated.TeamVisibility;
import net.raphimc.viabedrock.protocol.model.EntityAttribute;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerEntity extends LivingEntity {

    protected PlayerAbilities abilities;

    public PlayerEntity(final UserConnection user, final long runtimeId, final int javaId, final UUID javaUuid, final PlayerAbilities abilities) {
        super(user, abilities.entityUniqueId(), runtimeId, "minecraft:player", javaId, javaUuid, EntityTypes1_21_11.PLAYER);

        this.abilities = abilities;
    }

    public final void createTeam() {
        final PacketWrapper setPlayerTeam = PacketWrapper.create(ClientboundPackets1_21_11.SET_PLAYER_TEAM, this.user);
        setPlayerTeam.write(Types.STRING, "vb_" + this.javaId); // team name
        setPlayerTeam.write(Types.BYTE, (byte) PlayerTeamAction.ADD.ordinal()); // mode
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("vb_" + this.javaId)); // display name
        setPlayerTeam.write(Types.BYTE, (byte) 3); // flags
        setPlayerTeam.write(Types.VAR_INT, TeamVisibility.ALWAYS.ordinal()); // name tag visibility
        setPlayerTeam.write(Types.VAR_INT, TeamCollisionRule.NEVER.ordinal()); // collision rule
        setPlayerTeam.write(Types.VAR_INT, TextFormatting.RESET.getOrdinal()); // color
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("")); // prefix
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("")); // suffix
        setPlayerTeam.write(Types.STRING_ARRAY, new String[]{StringUtil.encodeUUID(this.javaUuid)}); // players
        setPlayerTeam.send(BedrockProtocol.class);
    }

    public final void updateName(final String name) {
        this.setName(name);

        final PacketWrapper setPlayerTeam = PacketWrapper.create(ClientboundPackets1_21_11.SET_PLAYER_TEAM, this.user);
        setPlayerTeam.write(Types.STRING, "vb_" + this.javaId); // team name
        setPlayerTeam.write(Types.BYTE, (byte) PlayerTeamAction.CHANGE.ordinal()); // mode
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("vb_" + this.javaId)); // display name
        setPlayerTeam.write(Types.BYTE, (byte) 3); // flags
        setPlayerTeam.write(Types.VAR_INT, TeamVisibility.ALWAYS.ordinal()); // name tag visibility
        setPlayerTeam.write(Types.VAR_INT, TeamCollisionRule.NEVER.ordinal()); // collision rule
        setPlayerTeam.write(Types.VAR_INT, TextFormatting.RESET.getOrdinal()); // color
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt(name)); // prefix
        setPlayerTeam.write(Types.TAG, TextUtil.stringToNbt("")); // suffix
        setPlayerTeam.send(BedrockProtocol.class);
    }

    @Override
    public void remove() {
        super.remove();

        final PacketWrapper setPlayerTeam = PacketWrapper.create(ClientboundPackets1_21_11.SET_PLAYER_TEAM, this.user);
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

    @Override
    protected boolean translateAttribute(final EntityAttribute attribute, final PacketWrapper javaAttributes, final AtomicInteger attributeCount, final List<EntityData> javaEntityData) {
        return switch (attribute.name()) {
            case "minecraft:absorption" -> {
                javaEntityData.add(new EntityData(this.getJavaEntityDataIndex("PLAYER_ABSORPTION"), VersionedTypes.V1_21_11.entityDataTypes.floatType, attribute.computeClampedValue()));
                javaAttributes.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEntityAttributes().get("minecraft:max_absorption")); // attribute id
                javaAttributes.write(Types.DOUBLE, (double) attribute.maxValue()); // base value
                javaAttributes.write(Types.VAR_INT, 0); // modifier count
                attributeCount.incrementAndGet();
                yield true;
            }
            case "minecraft:luck" -> {
                javaAttributes.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEntityAttributes().get("minecraft:luck")); // attribute id
                javaAttributes.write(Types.DOUBLE, (double) attribute.computeClampedValue()); // base value
                javaAttributes.write(Types.VAR_INT, 0); // modifier count
                attributeCount.incrementAndGet();
                yield true;
            }
            case "minecraft:player.hunger", "minecraft:player.saturation", "minecraft:player.experience", "minecraft:player.level", "minecraft:player.exhaustion" ->
                    true; // Ignore for non client player entities
            default -> super.translateAttribute(attribute, javaAttributes, attributeCount, javaEntityData);
        };
    }

}
