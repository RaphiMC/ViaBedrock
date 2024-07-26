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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AbilitiesIndex;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.SerializedAbilitiesData_SerializedAbilitiesLayer;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class PlayerAbilitiesType extends Type<PlayerAbilities> {

    public PlayerAbilitiesType() {
        super(PlayerAbilities.class);
    }

    @Override
    public PlayerAbilities read(ByteBuf buffer) {
        final long uniqueEntityId = buffer.readLongLE();
        final byte playerPermission = buffer.readByte();
        final byte commandPermission = buffer.readByte();

        final int layerCount = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final Map<SerializedAbilitiesData_SerializedAbilitiesLayer, PlayerAbilities.AbilitiesLayer> abilityLayers = new EnumMap<>(SerializedAbilitiesData_SerializedAbilitiesLayer.class);
        for (int i = 0; i < layerCount; i++) {
            final SerializedAbilitiesData_SerializedAbilitiesLayer layer = SerializedAbilitiesData_SerializedAbilitiesLayer.getByValue(buffer.readUnsignedShortLE(), SerializedAbilitiesData_SerializedAbilitiesLayer.CustomCache);
            final Set<AbilitiesIndex> abilitiesSet = this.getAbilities(buffer.readUnsignedIntLE());
            final Set<AbilitiesIndex> abilityValues = this.getAbilities(buffer.readUnsignedIntLE());
            final float flySpeed = buffer.readFloatLE();
            final float walkSpeed = buffer.readFloatLE();
            if (!abilityLayers.containsKey(layer)) {
                abilityLayers.put(layer, new PlayerAbilities.AbilitiesLayer(abilitiesSet, abilityValues, walkSpeed, flySpeed));
            }
        }

        return new PlayerAbilities(uniqueEntityId, playerPermission, commandPermission, abilityLayers);
    }

    @Override
    public void write(ByteBuf buffer, PlayerAbilities value) {
        buffer.writeLongLE(value.uniqueEntityId());
        buffer.writeByte(value.playerPermission());
        buffer.writeByte(value.commandPermission());

        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.abilityLayers().size());
        for (final Map.Entry<SerializedAbilitiesData_SerializedAbilitiesLayer, PlayerAbilities.AbilitiesLayer> entry : value.abilityLayers().entrySet()) {
            buffer.writeShortLE(entry.getKey().getValue());
            buffer.writeIntLE((int) this.getBits(entry.getValue().abilitiesSet()));
            buffer.writeIntLE((int) this.getBits(entry.getValue().abilityValues()));
            buffer.writeFloatLE(entry.getValue().flySpeed());
            buffer.writeFloatLE(entry.getValue().walkSpeed());
        }
    }

    private Set<AbilitiesIndex> getAbilities(final long bits) {
        final Set<AbilitiesIndex> abilities = EnumSet.noneOf(AbilitiesIndex.class);
        for (AbilitiesIndex index : AbilitiesIndex.values()) {
            if (index.getValue() >= 0 && (bits & (1L << index.getValue())) != 0) {
                abilities.add(index);
            }
        }
        return abilities;
    }

    private long getBits(final Set<AbilitiesIndex> abilities) {
        long bits = 0;
        for (AbilitiesIndex index : abilities) {
            bits |= 1L << index.getValue();
        }
        return bits;
    }

}
