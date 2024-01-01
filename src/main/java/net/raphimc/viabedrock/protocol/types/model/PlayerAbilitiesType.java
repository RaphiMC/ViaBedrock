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
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.PlayerAbilities;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class PlayerAbilitiesType extends Type<PlayerAbilities> {

    public PlayerAbilitiesType() {
        super(PlayerAbilities.class);
    }

    @Override
    public PlayerAbilities read(ByteBuf buffer) throws Exception {
        final long uniqueEntityId = buffer.readLongLE();
        final int playerPermission = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final int commandPermission = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);

        final int layerCount = BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer);
        final Int2ObjectMap<PlayerAbilities.Abilities> abilityLayers = new Int2ObjectOpenHashMap<>(layerCount);
        for (int i = 0; i < layerCount; i++) {
            final short type = buffer.readShortLE();
            final int abilitiesSet = buffer.readIntLE();
            final int abilityValues = buffer.readIntLE();
            final float flySpeed = buffer.readFloatLE();
            final float walkSpeed = buffer.readFloatLE();
            abilityLayers.put(type, new PlayerAbilities.Abilities(abilitiesSet, abilityValues, walkSpeed, flySpeed));
        }

        return new PlayerAbilities(uniqueEntityId, playerPermission, commandPermission, abilityLayers);
    }

    @Override
    public void write(ByteBuf buffer, PlayerAbilities value) throws Exception {
        buffer.writeLongLE(value.uniqueEntityId());
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.playerPermission());
        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.commandPermission());

        BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, value.abilityLayers().size());
        for (final Int2ObjectMap.Entry<PlayerAbilities.Abilities> entry : value.abilityLayers().int2ObjectEntrySet()) {
            buffer.writeShortLE(entry.getIntKey());
            buffer.writeIntLE(entry.getValue().abilitiesSet());
            buffer.writeIntLE(entry.getValue().abilityValues());
            buffer.writeFloatLE(entry.getValue().flySpeed());
            buffer.writeFloatLE(entry.getValue().walkSpeed());
        }
    }

}
