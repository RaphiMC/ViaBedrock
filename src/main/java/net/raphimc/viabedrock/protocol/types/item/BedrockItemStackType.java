/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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

import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.api.io.LittleEndianByteBufInputStream;
import net.raphimc.viabedrock.api.io.LittleEndianByteBufOutputStream;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.IOException;

public class BedrockItemStackType extends Type<BedrockItem> {

    private final int MAX_DEPTH = 16;
    private final int MAX_LIST_SIZE = 10240;
    private final int MAX_STACK_TAG_LENGTH = 64;

    private final int blockingId;
    private final Int2ObjectMap<IntSortedSet> blockItemValidBlockStates;
    private final boolean writeItemNetId;

    public BedrockItemStackType(final int blockingId, final Int2ObjectMap<IntSortedSet> blockItemValidBlockStates, final boolean writeItemNetId) {
        super(BedrockItem.class);

        this.blockingId = blockingId;
        this.blockItemValidBlockStates = blockItemValidBlockStates;
        this.writeItemNetId = writeItemNetId;
    }

    @Override
    public BedrockItem read(ByteBuf buffer) {
        final int id = BedrockTypes.SHORT_LE.read(buffer);

        final BedrockItem item = new BedrockItem(id);
        item.setAmount(buffer.readUnsignedShortLE());
        item.setData(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        if (buffer.readBoolean()) {
            BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // net id type

            item.setNetId(BedrockTypes.VAR_INT.read(buffer));
        }
        item.setBlockRuntimeId(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));

        final IntSortedSet validBlockStates = this.blockItemValidBlockStates.get(item.identifier());
        if (validBlockStates != null) { // Block item
            item.setData(0);
            if (!validBlockStates.contains(item.blockRuntimeId())) {
                item.setBlockRuntimeId(validBlockStates.firstInt());
            }
        } else { // Meta item
            item.setBlockRuntimeId(0);
        }

        String[] canPlace;
        String[] canBreak;

        final ByteBuf userData = buffer.readSlice(BedrockTypes.UNSIGNED_VAR_INT.read(buffer));
        if (userData.isReadable()) {
            try (LittleEndianByteBufInputStream stream = new LittleEndianByteBufInputStream(userData)) {
                int nbtSize = stream.readShort();

                if (nbtSize > 0) {
                    item.setTag(CompoundTag.read(stream, TagLimiter.noop(), MAX_DEPTH));
                } else if (nbtSize == -1) {
                    int tagCount = stream.readUnsignedByte();
                    if (tagCount != 1) throw new IllegalArgumentException("Expected 1 tag but got " + tagCount);
                    item.setTag(CompoundTag.read(stream, TagLimiter.noop(), MAX_DEPTH));
                }

                int maxLength = MAX_LIST_SIZE;
                int length = stream.readInt();
                if (length > maxLength) {
                    throw new IOException("String length " + length + " exceeds maximum of " + maxLength);
                }
                canPlace = new String[length];
                for (int i = 0; i < canPlace.length; i++) {
                    canPlace[i] = stream.readUTFMaxLen(MAX_STACK_TAG_LENGTH);
                }
                item.setCanPlace(canPlace);

                length = stream.readInt();
                if (length > maxLength) {
                    throw new IOException("String length " + length + " exceeds maximum of " + maxLength);
                }
                canBreak = new String[length];
                for (int i = 0; i < canBreak.length; i++) {
                    canBreak[i] = stream.readUTFMaxLen(MAX_STACK_TAG_LENGTH);
                }
                item.setCanBreak(canBreak);

                if (item.identifier() == this.blockingId) {
                    item.setBlockingTicks(stream.readLong());
                }

            } catch (IOException e) {
                throw new IllegalStateException("Failed to read item user data", e);
            }
        }

        return item;
    }

    @Override
    public void write(ByteBuf buffer, BedrockItem value) {
        BedrockTypes.SHORT_LE.write(buffer, (short) value.identifier());
        buffer.writeShortLE(value.amount());
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, (int) value.data());
        if (this.writeItemNetId) {
            buffer.writeBoolean(value.netId() != null);
            if (value.netId() != null) {
                BedrockTypes.UNSIGNED_VAR_INT.write(buffer, 0); // TODO: variant: oneOf<ItemStackNetId, ItemStackRequestId, ItemStackLegacyRequestId> (all read the same but is there difference in behavior?)
                BedrockTypes.VAR_INT.write(buffer, value.netId());
            }
        } else {
            buffer.writeBoolean(false);
        }
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.blockRuntimeId());

        if (value.isEmpty()) {
            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, 0);
            return;
        }

        final ByteBuf userData = buffer.alloc().buffer();
        try (LittleEndianByteBufOutputStream stream = new LittleEndianByteBufOutputStream(userData)) {
            if (value.tag() != null) {
                stream.writeShort(-1);
                stream.writeByte(1); // Hardcoded in current version
                value.tag().write(stream);
            } else {
                userData.writeShortLE(0);
            }

            String[] canPlace = value.canPlace();
            stream.writeInt(canPlace.length);
            for (String aCanPlace : canPlace) {
                stream.writeUTF(aCanPlace);
            }

            String[] canBreak = value.canBreak();
            stream.writeInt(canBreak.length);
            for (String aCanBreak : canBreak) {
                stream.writeUTF(aCanBreak);
            }

            if (value.identifier() == this.blockingId) {
                stream.writeLong(value.blockingTicks());
            }

            BedrockTypes.UNSIGNED_VAR_INT.write(buffer, userData.readableBytes());
            buffer.writeBytes(userData);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write item user data", e);
        } finally {
            userData.release();
        }
    }

}
