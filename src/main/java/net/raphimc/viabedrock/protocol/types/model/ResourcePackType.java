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
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

public class ResourcePackType extends Type<ResourcePack> {

    public ResourcePackType() {
        super(ResourcePack.class);
    }

    @Override
    public ResourcePack read(ByteBuf buffer) {
        final String id = BedrockTypes.STRING.read(buffer);
        final String version = BedrockTypes.STRING.read(buffer);
        final long size = buffer.readLongLE();
        final String contentKey = BedrockTypes.STRING.read(buffer);
        final String subPackName = BedrockTypes.STRING.read(buffer);
        final String contentId = BedrockTypes.STRING.read(buffer);
        final boolean hasScripts = buffer.readBoolean();
        final boolean isAddonPack = buffer.readBoolean();
        final boolean raytracingCapable = buffer.readBoolean();
        URL cdnUrl = null;
        try {
            final String cdnUrlString = BedrockTypes.STRING.read(buffer);
            if (!cdnUrlString.isEmpty()) {
                cdnUrl = new URL(cdnUrlString);
            }
        } catch (MalformedURLException ignored) {
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid resource pack UUID: " + id, e);
            uuid = new UUID(0L, 0L);
        }

        return new ResourcePack(uuid, version, contentKey, subPackName, contentId, hasScripts, isAddonPack, raytracingCapable, cdnUrl, size, PackType.Resources);
    }

    @Override
    public void write(ByteBuf buffer, ResourcePack value) {
        BedrockTypes.STRING.write(buffer, value.packId().toString());
        BedrockTypes.STRING.write(buffer, value.version());
        buffer.writeLongLE(value.compressedDataLength());
        BedrockTypes.STRING.write(buffer, value.contentKey());
        BedrockTypes.STRING.write(buffer, value.subPackName());
        BedrockTypes.STRING.write(buffer, value.contentId());
        buffer.writeBoolean(value.hasScripts());
        buffer.writeBoolean(value.isAddonPack());
        buffer.writeBoolean(value.raytracingCapable());
        if (value.cdnUrl() != null) {
            BedrockTypes.STRING.write(buffer, value.cdnUrl().toString());
        } else {
            BedrockTypes.STRING.write(buffer, "");
        }
    }

}
