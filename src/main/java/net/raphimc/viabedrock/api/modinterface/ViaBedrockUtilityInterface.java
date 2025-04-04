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
package net.raphimc.viabedrock.api.modinterface;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorDataIDs;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.types.primitive.ImageType;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ViaBedrockUtilityInterface {

    // This channel WILL ONLY be used to confirm that ViaBedrockUtility is present, the server will use viabedrockutility:data to respond
    public static final String CONFIRM_CHANNEL = "viabedrockutility:confirm";

    public static final String CHANNEL = "viabedrockutility:data";
    private static final int MAX_PAYLOAD_SIZE = 1048576;

    public static void confirmPresence(final UserConnection user) {
        final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundConfigurationPackets1_21.CUSTOM_PAYLOAD, user);
        pluginMessage.write(Types.STRING, CHANNEL); // Channel
        pluginMessage.write(Types.INT, PayloadType.CONFIRM.ordinal()); // Type
        pluginMessage.send(BedrockProtocol.class);
    }

    public static void spawnCustomEntity(final UserConnection user, final UUID uuid, final String identifier, final long bitmask, final Map<ActorDataIDs, EntityData> entityData) {
        final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_21_5.CUSTOM_PAYLOAD, user);
        pluginMessage.write(Types.STRING, CHANNEL); // Channel
        pluginMessage.write(Types.INT, PayloadType.MODEL_REQUEST.ordinal()); // Type
        writeString(pluginMessage, identifier);
        pluginMessage.write(Types.LONG, bitmask);

        boolean writeVariant = entityData.containsKey(ActorDataIDs.VARIANT);
        pluginMessage.write(Types.BOOLEAN, writeVariant);
        if (writeVariant) {
            pluginMessage.write(Types.INT, entityData.get(ActorDataIDs.VARIANT).<Integer>value());
        }

        boolean writeMarkVariant = entityData.containsKey(ActorDataIDs.MARK_VARIANT);
        pluginMessage.write(Types.BOOLEAN, writeMarkVariant);
        if (writeMarkVariant) {
            pluginMessage.write(Types.INT, entityData.get(ActorDataIDs.MARK_VARIANT).<Integer>value());
        }

        pluginMessage.write(Types.UUID, uuid);
        pluginMessage.send(BedrockProtocol.class);
    }

    public static void sendSkin(final UserConnection user, final UUID uuid, final SkinData skin) {
        if (skin.skinData() == null || skin.persona()) {
            return;
        }

        final boolean hasGeometry = !skin.geometryData().isEmpty() && !skin.geometryData().toLowerCase(Locale.ROOT).equals("null");
        final byte[] skinData = ImageType.getImageData(skin.skinData());
        final int maxPayloadSize = MAX_PAYLOAD_SIZE - 24;
        final int chunkCount = (int) Math.ceil(skinData.length / (double) maxPayloadSize);

        {
            final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_21_5.CUSTOM_PAYLOAD, user);
            pluginMessage.write(Types.STRING, CHANNEL); // Channel
            pluginMessage.write(Types.INT, PayloadType.SKIN_INFORMATION.ordinal());
            pluginMessage.write(Types.UUID, uuid);
            pluginMessage.write(Types.INT, skin.skinData().getWidth());
            pluginMessage.write(Types.INT, skin.skinData().getHeight());

            writeString(pluginMessage, skin.skinResourcePatch());
            pluginMessage.write(Types.BOOLEAN, hasGeometry);
            if (hasGeometry) {
                writeString(pluginMessage, skin.geometryData());
            }

            pluginMessage.write(Types.INT, chunkCount);
            pluginMessage.send(BedrockProtocol.class);
        }
        for (int i = 0; i < chunkCount; i++) {
            final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_21_5.CUSTOM_PAYLOAD, user);
            pluginMessage.write(Types.STRING, CHANNEL); // Channel
            pluginMessage.write(Types.INT, PayloadType.SKIN_DATA.ordinal());
            pluginMessage.write(Types.UUID, uuid);
            pluginMessage.write(Types.INT, i);
            if (chunkCount == 1) { // Fast path
                pluginMessage.write(Types.REMAINING_BYTES, skinData);
            } else {
                pluginMessage.write(Types.REMAINING_BYTES, Arrays.copyOfRange(skinData, i * maxPayloadSize, Math.min((i + 1) * maxPayloadSize, skinData.length)));
            }
            pluginMessage.send(BedrockProtocol.class);
        }
        if (skin.capeData() != null) {
            final byte[] capeData = ImageType.getImageData(skin.capeData());

            final PacketWrapper pluginMessage = PacketWrapper.create(ClientboundPackets1_21_5.CUSTOM_PAYLOAD, user);
            pluginMessage.write(Types.STRING, CHANNEL); // Channel
            pluginMessage.write(Types.INT, PayloadType.CAPE.ordinal());
            pluginMessage.write(Types.UUID, uuid);
            pluginMessage.write(Types.INT, skin.capeData().getWidth());
            pluginMessage.write(Types.INT, skin.capeData().getHeight());
            writeString(pluginMessage, skin.capeId());
            pluginMessage.write(Types.INT, capeData.length);
            pluginMessage.write(Types.REMAINING_BYTES, capeData);
            pluginMessage.send(BedrockProtocol.class);
        }
    }

    private static void writeString(final PacketWrapper wrapper, final String s) {
        wrapper.write(Types.INT, s.length());
        wrapper.write(Types.REMAINING_BYTES, s.getBytes(StandardCharsets.UTF_8));
    }

    private enum PayloadType {
        CONFIRM, MODEL_REQUEST, ANIMATE,
        CAPE, SKIN_INFORMATION, SKIN_DATA
    }
}
