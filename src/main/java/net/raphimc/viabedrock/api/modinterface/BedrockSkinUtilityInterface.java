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
package net.raphimc.viabedrock.api.modinterface;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.types.primitive.ImageType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class BedrockSkinUtilityInterface {

    public static final String CHANNEL = "bedrockskin:data";
    private static final int VERSION = 1;
    private static final int MESSAGE_CAPE = 0;
    private static final int MESSAGE_SKIN_INFORMATION = 1;
    private static final int MESSAGE_SKIN_DATA = 2;

    private static final int MAX_PAYLOAD_SIZE = 1048576;

    public static void sendSkin(final UserConnection user, final UUID uuid, final SkinData skin) throws Exception {
        if (skin.skinData() == null || skin.persona()) {
            return;
        }

        final boolean hasGeometry = !skin.geometryData().isEmpty() && !skin.geometryData().toLowerCase(Locale.ROOT).equals("null");
        final byte[] skinData = ImageType.getImageData(skin.skinData());
        final int maxPayloadSize = MAX_PAYLOAD_SIZE - 24;
        final int chunkCount = (int) Math.ceil(skinData.length / (double) maxPayloadSize);

        {
            final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_20_3.PLUGIN_MESSAGE, user);
            pluginMessage.write(Type.STRING, CHANNEL); // Channel
            pluginMessage.write(Type.INT, MESSAGE_SKIN_INFORMATION);
            pluginMessage.write(Type.INT, VERSION);
            pluginMessage.write(Type.UUID, uuid);
            pluginMessage.write(Type.INT, skin.skinData().getWidth());
            pluginMessage.write(Type.INT, skin.skinData().getHeight());
            pluginMessage.write(Type.BOOLEAN, hasGeometry);
            if (hasGeometry) {
                writeString(pluginMessage, skin.geometryData());
                writeString(pluginMessage, skin.skinResourcePatch());
            }

            pluginMessage.write(Type.INT, chunkCount);
            pluginMessage.send(BedrockProtocol.class);
        }
        for (int i = 0; i < chunkCount; i++) {
            final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_20_3.PLUGIN_MESSAGE, user);
            pluginMessage.write(Type.STRING, CHANNEL); // Channel
            pluginMessage.write(Type.INT, MESSAGE_SKIN_DATA);
            pluginMessage.write(Type.UUID, uuid);
            pluginMessage.write(Type.INT, i);
            if (chunkCount == 1) { // Fast path
                pluginMessage.write(Type.REMAINING_BYTES, skinData);
            } else {
                pluginMessage.write(Type.REMAINING_BYTES, Arrays.copyOfRange(skinData, i * maxPayloadSize, Math.min((i + 1) * maxPayloadSize, skinData.length)));
            }
            pluginMessage.send(BedrockProtocol.class);
        }
        if (skin.capeData() != null) {
            final byte[] capeData = ImageType.getImageData(skin.capeData());

            final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_20_3.PLUGIN_MESSAGE, user);
            pluginMessage.write(Type.STRING, CHANNEL); // Channel
            pluginMessage.write(Type.INT, MESSAGE_CAPE);
            pluginMessage.write(Type.INT, VERSION);
            pluginMessage.write(Type.UUID, uuid);
            pluginMessage.write(Type.INT, skin.capeData().getWidth());
            pluginMessage.write(Type.INT, skin.capeData().getHeight());
            writeString(pluginMessage, skin.capeId());
            pluginMessage.write(Type.INT, capeData.length);
            pluginMessage.write(Type.REMAINING_BYTES, capeData);
            pluginMessage.send(BedrockProtocol.class);
        }
    }

    private static void writeString(final PacketWrapper wrapper, final String s) {
        wrapper.write(Type.INT, s.length());
        wrapper.write(Type.REMAINING_BYTES, s.getBytes(StandardCharsets.UTF_8));
    }

}
