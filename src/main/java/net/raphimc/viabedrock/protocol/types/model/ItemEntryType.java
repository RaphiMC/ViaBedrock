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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ItemVersion;
import net.raphimc.viabedrock.protocol.model.ItemEntry;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ItemEntryType extends Type<ItemEntry> {

    public ItemEntryType() {
        super(ItemEntry.class);
    }

    @Override
    public ItemEntry read(ByteBuf buffer) {
        final String identifier = Key.namespaced(BedrockTypes.STRING.read(buffer));
        final int id = buffer.readShortLE();
        final boolean componentBased = buffer.readBoolean();
        final ItemVersion version = ItemVersion.getByValue(BedrockTypes.VAR_INT.read(buffer), ItemVersion.None);
        final CompoundTag componentData = (CompoundTag) BedrockTypes.NETWORK_TAG.read(buffer);

        return new ItemEntry(identifier, id, componentBased, version, componentData);
    }

    @Override
    public void write(ByteBuf buffer, ItemEntry value) {
        BedrockTypes.STRING.write(buffer, value.identifier());
        buffer.writeShortLE(value.id());
        buffer.writeBoolean(value.componentBased());
        BedrockTypes.VAR_INT.write(buffer, value.version().getValue());
        BedrockTypes.NETWORK_TAG.write(buffer, value.componentData());
    }

}
