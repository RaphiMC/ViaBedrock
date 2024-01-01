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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Triple;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.logging.Level;

public class PackIdAndVersionAndNameType extends Type<Triple<UUID, String, String>> {

    public PackIdAndVersionAndNameType() {
        super("PackID and Version and Name", Triple.class);
    }

    @Override
    public Triple<UUID, String, String> read(ByteBuf buffer) throws Exception {
        final String packId = BedrockTypes.STRING.read(buffer); // pack id
        final String packVersion = BedrockTypes.STRING.read(buffer); // pack version

        UUID packUUID;
        try {
            packUUID = java.util.UUID.fromString(packId);
        } catch (IllegalArgumentException e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid pack UUID: " + packId, e);
            packUUID = new UUID(0L, 0L);
        }

        final String packName = BedrockTypes.STRING.read(buffer); // pack name
        return new Triple<>(packUUID, packVersion, packName);
    }

    @Override
    public void write(ByteBuf buffer, Triple<UUID, String, String> value) throws Exception {
        BedrockTypes.STRING.write(buffer, value.first().toString()); // pack id
        BedrockTypes.STRING.write(buffer, value.second()); // pack version
        BedrockTypes.STRING.write(buffer, value.third()); // pack name
    }

}
