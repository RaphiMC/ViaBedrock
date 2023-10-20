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
package net.raphimc.viabedrock.protocol.types.item;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.logging.Level;

public class BedrockItemType extends Type<BedrockItem> {

    private final int blockingId;

    public BedrockItemType(final int blockingId) {
        super(BedrockItem.class);

        this.blockingId = blockingId;
    }

    @Override
    public BedrockItem read(ByteBuf buffer) throws Exception {
        final int id = BedrockTypes.VAR_INT.read(buffer);
        if (id == 0) {
            return null;
        }

        final BedrockItem item = new BedrockItem(id);
        item.setAmount(buffer.readUnsignedShortLE());
        item.setData(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        item.setUsingNetId(buffer.readBoolean());
        if (item.usingNetId()) {
            item.setNetId(BedrockTypes.VAR_INT.read(buffer));
        }
        item.setBlockRuntimeId(BedrockTypes.VAR_INT.read(buffer));

        final ByteBuf extraData = buffer.readSlice(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));

        final int tagLength = extraData.readShortLE();
        if (tagLength > 0) {
            item.setTag((CompoundTag) BedrockTypes.TAG_LE.read(extraData));
        } else if (tagLength == -1) {
            final int tagCount = extraData.readUnsignedByte();
            if (tagCount != 1) {
                throw new IllegalArgumentException("Expected 1 tag but got " + tagCount);
            }
            item.setTag((CompoundTag) BedrockTypes.TAG_LE.read(extraData));
        }

        item.setCanPlace(BedrockTypes.UTF8_STRING_ARRAY.read(extraData));
        item.setCanBreak(BedrockTypes.UTF8_STRING_ARRAY.read(extraData));

        if (item.identifier() == this.blockingId) {
            item.setBlockingTicks(extraData.readLongLE());
        }

        if (extraData.isReadable()) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Item had too much data: " + extraData.readableBytes() + " extra bytes");
        }

        return item;
    }

    @Override
    public void write(ByteBuf buffer, BedrockItem value) throws Exception {
        if (value == null) {
            BedrockTypes.VAR_INT.write(buffer, 0);
            return;
        }

        BedrockTypes.VAR_INT.write(buffer, value.identifier());
        buffer.writeShortLE(value.amount());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, (int) value.data());
        buffer.writeBoolean(value.usingNetId());
        if (value.usingNetId()) {
            BedrockTypes.VAR_INT.write(buffer, value.netId());
        }
        BedrockTypes.VAR_INT.write(buffer, value.blockRuntimeId());

        final ByteBuf extraData = buffer.alloc().buffer();

        if (value.tag() != null) {
            extraData.writeShortLE(-1);
            extraData.writeByte(1);
            BedrockTypes.TAG_LE.write(extraData, value.tag());
        } else {
            extraData.writeShortLE(0);
        }

        BedrockTypes.UTF8_STRING_ARRAY.write(extraData, value.canPlace());
        BedrockTypes.UTF8_STRING_ARRAY.write(extraData, value.canBreak());

        if (value.identifier() == this.blockingId) {
            extraData.writeLongLE(value.blockingTicks());
        }

        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, extraData.readableBytes());
        buffer.writeBytes(extraData);
        extraData.release();
    }

}
