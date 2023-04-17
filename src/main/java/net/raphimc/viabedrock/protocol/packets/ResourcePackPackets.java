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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.Triple;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ResourcePackStatus;
import net.raphimc.viabedrock.protocol.model.ResourcePack;
import net.raphimc.viabedrock.protocol.providers.ResourcePackProvider;
import net.raphimc.viabedrock.protocol.rewriter.ResourcePackRewriter;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ResourcePackPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACKS_INFO, ClientboundPackets1_19_4.RESOURCE_PACK, wrapper -> {
            if (wrapper.user().has(ResourcePacksStorage.class)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource packs info twice");
                return;
            }

            final ResourcePacksStorage resourcePacksStorage = new ResourcePacksStorage(wrapper.user());
            wrapper.user().put(resourcePacksStorage);

            final boolean required = wrapper.read(Type.BOOLEAN); // must accept
            wrapper.read(Type.BOOLEAN); // scripting
            wrapper.read(Type.BOOLEAN); // server packs force enabled

            final ResourcePack[] behaviorPacks = wrapper.read(BedrockTypes.BEHAVIOUR_PACK_ARRAY);
            for (ResourcePack behaviorPack : behaviorPacks) {
                resourcePacksStorage.addPack(behaviorPack);
            }
            final ResourcePack[] resourcePacks = wrapper.read(BedrockTypes.RESOURCE_PACK_ARRAY);
            for (ResourcePack resourcePack : resourcePacks) {
                resourcePacksStorage.addPack(resourcePack);
            }

            if(wrapper.user().getProtocolInfo().getProtocolVersion() >= ProtocolVersion.v1_19_4.getVersion()) {
                ViaBedrock.getResourcePackServer().addConnection(resourcePacksStorage.getHttpToken(), wrapper.user());

                wrapper.write(Type.STRING, ViaBedrock.getResourcePackServer().getUrl() + "?token=" + resourcePacksStorage.getHttpToken().toString()); // url
                wrapper.write(Type.STRING, ""); // hash
                wrapper.write(Type.BOOLEAN, false); // requires accept
                wrapper.write(Type.OPTIONAL_COMPONENT, JsonUtil.textToComponent(
                        "\nIf you press 'Yes', the resource packs will be downloaded and converted to Java Edition format. " +
                                "This may take a up to 15 seconds, depending on your internet connection and the size of the packs.\n\n" +
                                "If you press 'No', you can join without loading the resource packs" + (required ? ", but you will have a worse gameplay experience." : ".")
                )); // prompt message
            } else {
                wrapper.cancel();
                final PacketWrapper resourcePackStatus = PacketWrapper.create(ServerboundPackets1_19_4.RESOURCE_PACK_STATUS, wrapper.user());
                resourcePackStatus.write(Type.VAR_INT, 1); // status | 1 = DECLINED
                resourcePackStatus.sendToServer(BedrockProtocol.class, false);
            }
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

            if (resourcePacksStorage.hasPack(idAndVersion.key())) {
                final ResourcePack resourcePack = resourcePacksStorage.getPack(idAndVersion.key());
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

            if (resourcePacksStorage.hasPack(idAndVersion.key()) && !resourcePacksStorage.getPack(idAndVersion.key()).isDecompressed()) {
                final ResourcePack resourcePack = resourcePacksStorage.getPack(idAndVersion.key());
                if (resourcePack.processDataChunk(chunkIndex, data) && ViaBedrock.getConfig().storePacks()) {
                    Via.getManager().getProviders().get(ResourcePackProvider.class).addPack(resourcePack);
                }
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

            wrapper.read(Type.BOOLEAN); // must accept
            final Triple<UUID, String, String>[] behaviourPacks = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION_AND_NAME_ARRAY); // behaviour packs
            final Triple<UUID, String, String>[] resourcePacks = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION_AND_NAME_ARRAY); // resource packs
            wrapper.read(BedrockTypes.STRING); // game version
            wrapper.read(BedrockTypes.EXPERIMENT_ARRAY); // experiments
            wrapper.read(Type.BOOLEAN); // experiments previously toggled

            final UUID[] behaviourPackIds = new UUID[behaviourPacks.length];
            for (int i = 0; i < behaviourPacks.length; i++) {
                behaviourPackIds[i] = behaviourPacks[i].first();
            }
            final UUID[] resourcePackIds = new UUID[resourcePacks.length];
            for (int i = 0; i < resourcePacks.length; i++) {
                resourcePackIds[i] = resourcePacks[i].first();
            }

            if (resourcePacksStorage.areAllPacksDecompressed() && resourcePacksStorage.getHttpConsumer() != null) {
                final long start = System.currentTimeMillis();
                final ResourcePack.Content javaContent = ResourcePackRewriter.bedrockToJava(resourcePacksStorage, resourcePackIds, behaviourPackIds);
                Via.getPlatform().getLogger().log(Level.INFO, "Converted packs in " + (System.currentTimeMillis() - start) + "ms");

                resourcePacksStorage.getHttpConsumer().accept(javaContent.toZip());
                resourcePacksStorage.setHttpConsumer(null);
            } else {
                final PacketWrapper resourcePackClientResponse = wrapper.create(ClientboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
                resourcePackClientResponse.write(Type.UNSIGNED_BYTE, ResourcePackStatus.COMPLETED); // status
                resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                resourcePackClientResponse.sendToServer(BedrockProtocol.class);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_19_4.RESOURCE_PACK_STATUS, ServerboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE, wrapper -> {
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);

            final int status = wrapper.read(Type.VAR_INT); // status
            switch (status) {
                case 0: // SUCCESSFULLY_LOADED
                    wrapper.write(Type.UNSIGNED_BYTE, ResourcePackStatus.COMPLETED); // status
                    wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                    break;
                case 2: // FAILED_DOWNLOAD
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Client resource pack download/application failed");
                case 1: // DECLINED
                    resourcePacksStorage.setCompleted(true);
                    wrapper.write(Type.UNSIGNED_BYTE, ResourcePackStatus.HAVE_ALL_PACKS); // status
                    wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                    break;
                case 3: // ACCEPTED
                    final Set<String> missingPacks = new HashSet<>();
                    for (ResourcePack pack : resourcePacksStorage.getPacks()) {
                        if (ViaBedrock.getConfig().storePacks() && Via.getManager().getProviders().get(ResourcePackProvider.class).hasPack(pack)) {
                            Via.getManager().getProviders().get(ResourcePackProvider.class).loadPack(pack);
                        } else {
                            missingPacks.add(pack.packId() + "_" + pack.version());
                        }
                    }

                    if (!missingPacks.isEmpty()) {
                        ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Downloading " + missingPacks.size() + " packs");
                        wrapper.write(Type.UNSIGNED_BYTE, ResourcePackStatus.SEND_PACKS); // status
                        wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, missingPacks.toArray(new String[0])); // pack ids
                    } else {
                        resourcePacksStorage.setCompleted(true);
                        wrapper.write(Type.UNSIGNED_BYTE, ResourcePackStatus.HAVE_ALL_PACKS); // status
                        wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                    }
                    break;
                default:
                    BedrockProtocol.kickForIllegalState(wrapper.user(), "Unknown resource pack status: " + status);
            }
        });
    }

}
