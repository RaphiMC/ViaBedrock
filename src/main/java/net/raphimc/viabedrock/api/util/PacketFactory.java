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
package net.raphimc.viabedrock.api.util;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonNull;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

public class PacketFactory {

    public static PacketWrapper systemChat(final UserConnection user, final Tag message) {
        final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_20_3.SYSTEM_CHAT, user);
        systemChat.write(Type.TAG, message); // message
        systemChat.write(Type.BOOLEAN, false); // overlay
        return systemChat;
    }

    public static PacketWrapper blockEntityData(final UserConnection user, final Position position, final BlockEntity blockEntity) {
        final PacketWrapper blockEntityData = PacketWrapper.create(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, user);
        blockEntityData.write(Type.POSITION1_14, position); // position
        blockEntityData.write(Type.VAR_INT, blockEntity.typeId()); // type
        blockEntityData.write(Type.COMPOUND_TAG, blockEntity.tag()); // block entity tag
        return blockEntityData;
    }

    public static <T extends Throwable> void sendSystemChat(final UserConnection user, final Tag message) throws T {
        try {
            systemChat(user, message).send(BedrockProtocol.class);
        } catch (Throwable e) {
            throw (T) e;
        }
    }

    public static <T extends Throwable> void sendBlockEntityData(final UserConnection user, final Position position, final BlockEntity blockEntity) throws T {
        try {
            blockEntityData(user, position, blockEntity).send(BedrockProtocol.class);
        } catch (Throwable e) {
            throw (T) e;
        }
    }

    public static void writeDisconnect(final PacketWrapper wrapper, final String reason) {
        switch (wrapper.getPacketType().state()) {
            case LOGIN:
                wrapper.write(Type.COMPONENT, reason != null ? TextUtil.stringToGson(reason) : JsonNull.INSTANCE);
                break;
            case CONFIGURATION:
            case PLAY:
                wrapper.write(Type.TAG, reason != null ? TextUtil.stringToNbt(reason) : null);
                break;
            default:
                throw new IllegalStateException("Unexpected state: " + wrapper.getPacketType().state());
        }
    }

}
