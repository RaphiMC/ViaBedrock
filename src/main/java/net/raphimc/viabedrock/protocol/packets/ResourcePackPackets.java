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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ResourcePackStatus;
import net.raphimc.viabedrock.protocol.model.ResourcePack;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ResourcePackPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACKS_INFO, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (resourcePacksStorage.isCompleted()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource packs info after transfer completion");
                return;
            }

            wrapper.read(Type.BOOLEAN); // forced
            wrapper.read(Type.BOOLEAN); // scripting
            wrapper.read(Type.BOOLEAN); // server packs force enabled
            final ResourcePack[] behaviorPacks = wrapper.read(BedrockTypes.BEHAVIOUR_PACK_ARRAY);
            final ResourcePack[] resourcePacks = wrapper.read(BedrockTypes.RESOURCE_PACK_ARRAY);

            final Set<String> missingPacks = new HashSet<>();
            for (ResourcePack behaviorPack : behaviorPacks) {
                if (!resourcePacksStorage.hasBehaviorPack(behaviorPack.packId())) {
                    missingPacks.add(behaviorPack.packId() + "_" + behaviorPack.version());
                    resourcePacksStorage.addBehaviorPack(behaviorPack);
                }
            }
            for (ResourcePack resourcePack : resourcePacks) {
                if (!resourcePacksStorage.hasResourcePack(resourcePack.packId())) {
                    missingPacks.add(resourcePack.packId() + "_" + resourcePack.version());
                    resourcePacksStorage.addResourcePack(resourcePack);
                }
            }

            if (missingPacks.isEmpty()) {
                resourcePacksStorage.setCompleted(true);
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Requesting " + missingPacks.size() + " packs");
            }

            final PacketWrapper resourcePackClientResponse = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
            resourcePackClientResponse.write(Type.UNSIGNED_BYTE, missingPacks.isEmpty() ? ResourcePackStatus.HAVE_ALL_PACKS : ResourcePackStatus.SEND_PACKS); // status
            resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, missingPacks.toArray(new String[0])); // resource pack ids
            resourcePackClientResponse.sendToServer(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_DATA_INFO, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (resourcePacksStorage.isCompleted()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource pack data info after transfer completion");
                return;
            }

            final Pair<UUID, String> idAndVersion = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION); // pack id and version
            final int maxChunkSize = wrapper.read(BedrockTypes.UNSIGNED_INT_LE).intValue(); // max chunk size
            wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // chunk count | Ignored by Mojang client
            final long compressedPackSize = wrapper.read(BedrockTypes.LONG_LE); // compressed pack size
            final byte[] hash = wrapper.read(BedrockTypes.BYTE_ARRAY); // hash
            final boolean premium = wrapper.read(Type.BOOLEAN); // premium
            final short type = wrapper.read(Type.UNSIGNED_BYTE); // type
            final int actualChunkCount = (int) Math.ceil((double) compressedPackSize / maxChunkSize);

            if (resourcePacksStorage.hasResourcePack(idAndVersion.key())) {
                final ResourcePack resourcePack = resourcePacksStorage.getResourcePack(idAndVersion.key());
                resourcePack.setVersion(idAndVersion.value());
                resourcePack.setHash(hash);
                resourcePack.setPremium(premium);
                resourcePack.setType(type);
                resourcePack.setCompressedDataLength((int) compressedPackSize, maxChunkSize);
            } else { // Mojang client requests data anyway
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource pack data info for unknown pack: " + idAndVersion.key());
            }

            for (int i = 0; i < actualChunkCount; i++) {
                final PacketWrapper resourcePackChunkRequest = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CHUNK_REQUEST);
                resourcePackChunkRequest.write(BedrockTypes.PACK_ID_AND_VERSION, idAndVersion); // pack id and version
                resourcePackChunkRequest.write(BedrockTypes.INT_LE, i); // chunk index
                resourcePackChunkRequest.sendToServer(BedrockProtocol.class);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_CHUNK_DATA, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (resourcePacksStorage.isCompleted()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource pack chunk data after transfer completion");
                return;
            }

            final Pair<UUID, String> idAndVersion = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION); // pack id and version
            final int chunkIndex = wrapper.read(BedrockTypes.INT_LE); // chunk index
            wrapper.read(BedrockTypes.LONG_LE); // progress
            final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data

            if (resourcePacksStorage.hasResourcePack(idAndVersion.key())) {
                final ResourcePack resourcePack = resourcePacksStorage.getResourcePack(idAndVersion.key());
                resourcePack.setVersion(idAndVersion.value());
                resourcePack.processDataChunk(chunkIndex, data);
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource pack chunk data for unknown pack: " + idAndVersion.key());
            }

            if (resourcePacksStorage.areAllPacksDecompressed()) {
                resourcePacksStorage.setCompleted(true);
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "All packs have been decompressed and decrypted");
                final PacketWrapper resourcePackClientResponse = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
                resourcePackClientResponse.write(Type.UNSIGNED_BYTE, ResourcePackStatus.HAVE_ALL_PACKS); // status
                resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // resource pack ids
                resourcePackClientResponse.sendToServer(BedrockProtocol.class);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_STACK, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (!resourcePacksStorage.isCompleted()) {
                BedrockProtocol.kickForIllegalState(wrapper.user(), "Received resource pack stack before transfer completion");
                return;
            }

            final PacketWrapper resourcePackClientResponse = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
            resourcePackClientResponse.write(Type.UNSIGNED_BYTE, ResourcePackStatus.COMPLETED); // status
            resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // resource pack ids
            resourcePackClientResponse.sendToServer(BedrockProtocol.class);
        });
    }

}
