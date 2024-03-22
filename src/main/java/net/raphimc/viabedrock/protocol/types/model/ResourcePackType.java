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
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.ResourcePack;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.logging.Level;

public class ResourcePackType extends Type<ResourcePack> {

    public ResourcePackType() {
        super(ResourcePack.class);
    }

    @Override
    public ResourcePack read(ByteBuf buffer) throws Exception {
        final String packId = BedrockTypes.STRING.read(buffer);
        final String packVersion = BedrockTypes.STRING.read(buffer);
        final long packSize = buffer.readLongLE();
        final String contentKey = BedrockTypes.STRING.read(buffer);
        final String subPackName = BedrockTypes.STRING.read(buffer);
        final String contentId = BedrockTypes.STRING.read(buffer);
        final boolean scripting = buffer.readBoolean();
        final boolean raytracingCapable = buffer.readBoolean();

        java.util.UUID packUUID;
        try {
            packUUID = java.util.UUID.fromString(packId);
        } catch (IllegalArgumentException e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid resource pack UUID: " + packId, e);
            packUUID = new UUID(0L, 0L);
        }

        return new ResourcePack(packUUID, packVersion, contentKey, subPackName, contentId, scripting, raytracingCapable, packSize, PackType.Resources);
    }

    @Override
    public void write(ByteBuf buffer, ResourcePack value) throws Exception {
        BedrockTypes.STRING.write(buffer, value.packId().toString());
        BedrockTypes.STRING.write(buffer, value.version());
        buffer.writeLongLE(value.compressedDataLength());
        BedrockTypes.STRING.write(buffer, value.contentKey());
        BedrockTypes.STRING.write(buffer, value.subPackName());
        BedrockTypes.STRING.write(buffer, value.contentId());
        buffer.writeBoolean(value.scripting());
        buffer.writeBoolean(value.raytracingCapable());
    }

}
