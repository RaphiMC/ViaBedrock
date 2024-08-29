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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.Triple;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.http.BedrockPackDownloader;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ResourcePackResponse;
import net.raphimc.viabedrock.protocol.data.enums.java.ResourcePackAction;
import net.raphimc.viabedrock.protocol.model.Experiment;
import net.raphimc.viabedrock.protocol.provider.ResourcePackProvider;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ResourcePackPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientboundTransition(ClientboundBedrockPackets.RESOURCE_PACKS_INFO, ClientboundConfigurationPackets1_21.RESOURCE_PACK_PUSH, (PacketHandler) wrapper -> {
            wrapper.cancel();
            if (wrapper.user().has(ResourcePacksStorage.class)) {
                if (wrapper.user().get(ResourcePacksStorage.class).hasFinishedLoading()) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received RESOURCE_PACKS_INFO after loading completion");
                    return;
                }
            }

            final ResourcePacksStorage resourcePacksStorage = new ResourcePacksStorage(wrapper.user());
            wrapper.user().put(resourcePacksStorage);

            wrapper.read(Types.BOOLEAN); // must accept
            wrapper.read(Types.BOOLEAN); // has addons
            wrapper.read(Types.BOOLEAN); // has scripts
            wrapper.read(Types.BOOLEAN); // force server packs enabled

            final ResourcePack[] behaviorPacks = wrapper.read(BedrockTypes.BEHAVIOUR_PACK_ARRAY);
            for (ResourcePack behaviorPack : behaviorPacks) {
                resourcePacksStorage.addPack(behaviorPack);
            }
            final ResourcePack[] resourcePacks = wrapper.read(BedrockTypes.RESOURCE_PACK_ARRAY);
            for (ResourcePack resourcePack : resourcePacks) {
                resourcePacksStorage.addPack(resourcePack);
            }
            final int cdnEntriesCount = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // cdn entries count
            for (int i = 0; i < cdnEntriesCount; i++) {
                final Pair<UUID, String> idAndVersion = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION); // pack id
                final String url = wrapper.read(BedrockTypes.STRING); // remote url
                try {
                    if (resourcePacksStorage.hasPack(idAndVersion.key())) {
                        resourcePacksStorage.getPack(idAndVersion.key()).setUrl(new URL(url));
                    } else {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown CDN entry: " + idAndVersion.key() + " (" + url + ")");
                    }
                } catch (MalformedURLException ignored) {
                }
            }

            if (ViaBedrock.getConfig().shouldTranslateResourcePacks() && wrapper.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolConstants.JAVA_VERSION)) {
                final CompletableFuture<Void> httpFuture = resourcePacksStorage.runHttpTask(resourcePacksStorage.getPacks(), pack -> {
                    final BedrockPackDownloader downloader = new BedrockPackDownloader(pack.url());
                    final int contentLength = downloader.getContentLength();
                    pack.setCompressedDataLength(contentLength, contentLength);
                }, (pack, e) -> {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to get content length for pack: " + pack.packId() + " (" + pack.url() + ")", e);
                    pack.setUrl(null); // Use the old resource pack downloading method
                });

                final UUID httpToken = UUID.randomUUID();
                ViaBedrock.getResourcePackServer().addConnection(httpToken, wrapper.user());

                final PacketWrapper resourcePackPush = PacketWrapper.create(wrapper.getPacketType(), wrapper.user());
                resourcePackPush.write(Types.UUID, UUID.randomUUID()); // pack id
                resourcePackPush.write(Types.STRING, ViaBedrock.getResourcePackServer().getUrl() + "?token=" + httpToken); // url
                resourcePackPush.write(Types.STRING, ""); // hash
                resourcePackPush.write(Types.BOOLEAN, false); // requires accept
                resourcePackPush.write(Types.OPTIONAL_TAG, TextUtil.stringToNbt(
                        "\n§aIf you press 'Yes', the resource packs will be downloaded and converted to the Java Edition format. " +
                                "This may take a while, depending on your internet connection and the size of the packs. " +
                                "If you press 'No', you can join without loading the resource packs but you will have a worse gameplay experience.")
                ); // prompt message
                httpFuture.thenRun(() -> resourcePackPush.scheduleSend(BedrockProtocol.class));
            } else {
                final PacketWrapper resourcePack = PacketWrapper.create(ServerboundConfigurationPackets1_20_5.RESOURCE_PACK, wrapper.user());
                resourcePack.write(Types.UUID, UUID.randomUUID()); // pack id
                resourcePack.write(Types.VAR_INT, ResourcePackAction.DECLINED.ordinal()); // status
                resourcePack.sendToServer(BedrockProtocol.class, false);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_DATA_INFO, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (resourcePacksStorage.hasFinishedLoading()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received RESOURCE_PACK_DATA_INFO after loading completion");
                return;
            }

            final Pair<UUID, String> idAndVersion = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION); // pack id and version
            final int maxChunkSize = wrapper.read(BedrockTypes.UNSIGNED_INT_LE).intValue(); // max chunk size
            wrapper.read(BedrockTypes.UNSIGNED_INT_LE); // chunk count | Ignored by Bedrock client
            final long compressedPackSize = wrapper.read(BedrockTypes.LONG_LE); // compressed pack size
            final byte[] hash = wrapper.read(BedrockTypes.BYTE_ARRAY); // hash
            final boolean premium = wrapper.read(Types.BOOLEAN); // premium
            final PackType type = PackType.getByValue(wrapper.read(Types.BYTE), PackType.Invalid); // type
            final int actualChunkCount = (int) Math.ceil((double) compressedPackSize / maxChunkSize);

            if (resourcePacksStorage.hasPack(idAndVersion.key())) {
                final ResourcePack resourcePack = resourcePacksStorage.getPack(idAndVersion.key());
                resourcePack.setHash(hash);
                resourcePack.setPremium(premium);
                resourcePack.setType(type);
                resourcePack.setCompressedDataLength((int) compressedPackSize, maxChunkSize);
            } else { // Bedrock client requests data anyway
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received RESOURCE_PACK_DATA_INFO for unknown pack: " + idAndVersion.key());
            }

            for (int i = 0; i < actualChunkCount; i++) {
                final PacketWrapper resourcePackChunkRequest = wrapper.create(ServerboundBedrockPackets.RESOURCE_PACK_CHUNK_REQUEST);
                resourcePackChunkRequest.write(BedrockTypes.PACK_ID_AND_VERSION, idAndVersion); // pack id and version
                resourcePackChunkRequest.write(BedrockTypes.INT_LE, i); // chunk index
                resourcePackChunkRequest.sendToServer(BedrockProtocol.class);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_CHUNK_DATA, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (resourcePacksStorage.hasFinishedLoading()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received RESOURCE_PACK_CHUNK_DATA after loading completion");
                return;
            }

            final Pair<UUID, String> idAndVersion = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION); // pack id and version
            final int chunkIndex = wrapper.read(BedrockTypes.INT_LE); // chunk index
            wrapper.read(BedrockTypes.LONG_LE); // progress
            final byte[] data = wrapper.read(BedrockTypes.BYTE_ARRAY); // data

            if (resourcePacksStorage.hasPack(idAndVersion.key()) && !resourcePacksStorage.getPack(idAndVersion.key()).isDecompressed()) {
                final ResourcePack resourcePack = resourcePacksStorage.getPack(idAndVersion.key());
                try {
                    if (resourcePack.processDataChunk(chunkIndex, data)) {
                        Via.getManager().getProviders().get(ResourcePackProvider.class).addPack(resourcePack);
                        resourcePacksStorage.sendResponseIfAllDownloadsCompleted();
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to process RESOURCE_PACK_CHUNK_DATA for pack: " + idAndVersion.key(), e);
                }
            } else {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received RESOURCE_PACK_CHUNK_DATA for unknown pack: " + idAndVersion.key());
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.RESOURCE_PACK_STACK, null, wrapper -> {
            wrapper.cancel();
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);
            if (resourcePacksStorage.hasFinishedLoading()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received RESOURCE_PACK_STACK after loading completion");
                return;
            }

            wrapper.read(Types.BOOLEAN); // must accept
            final Triple<UUID, String, String>[] behaviourPacks = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION_AND_NAME_ARRAY); // behaviour packs
            final Triple<UUID, String, String>[] resourcePacks = wrapper.read(BedrockTypes.PACK_ID_AND_VERSION_AND_NAME_ARRAY); // resource packs
            wrapper.read(BedrockTypes.STRING); // game version
            final Experiment[] experiments = wrapper.read(BedrockTypes.EXPERIMENT_ARRAY); // experiments
            wrapper.read(Types.BOOLEAN); // experiments previously toggled
            wrapper.read(Types.BOOLEAN); // has editor packs

            for (Experiment experiment : experiments) {
                if (experiment.enabled()) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "This server uses an experimental resource pack: " + experiment.name());
                }
            }

            final UUID[] behaviourPackIds = new UUID[behaviourPacks.length];
            for (int i = 0; i < behaviourPacks.length; i++) {
                behaviourPackIds[i] = behaviourPacks[i].first();
            }
            final UUID[] resourcePackIds = new UUID[resourcePacks.length];
            for (int i = 0; i < resourcePacks.length; i++) {
                resourcePackIds[i] = resourcePacks[i].first();
            }
            resourcePacksStorage.setPackStack(resourcePackIds, behaviourPackIds);

            if (!resourcePacksStorage.isJavaClientWaitingForPack()) {
                final PacketWrapper resourcePackClientResponse = wrapper.create(ServerboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE);
                resourcePackClientResponse.write(Types.BYTE, (byte) ResourcePackResponse.ResourcePackStackFinished.getValue()); // status
                resourcePackClientResponse.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                resourcePackClientResponse.sendToServer(BedrockProtocol.class);
            }
        });

        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_5.RESOURCE_PACK, ServerboundBedrockPackets.RESOURCE_PACK_CLIENT_RESPONSE, wrapper -> {
            final ResourcePacksStorage resourcePacksStorage = wrapper.user().get(ResourcePacksStorage.class);

            wrapper.read(Types.UUID); // pack id
            final ResourcePackAction status = ResourcePackAction.values()[wrapper.read(Types.VAR_INT)]; // status
            switch (status) {
                case SUCCESSFULLY_LOADED -> {
                    if (!resourcePacksStorage.hasFinishedLoading()) {
                        wrapper.cancel();
                    }

                    resourcePacksStorage.setLoadedOnJavaClient();
                    wrapper.write(Types.BYTE, (byte) ResourcePackResponse.ResourcePackStackFinished.getValue()); // status
                    wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                }
                case FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD, DISCARDED -> {
                    if (!resourcePacksStorage.hasFinishedLoading()) {
                        wrapper.cancel();
                    }

                    resourcePacksStorage.setJavaClientWaitingForPack(false);
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Client resource pack download/loading failed");
                    wrapper.write(Types.BYTE, (byte) ResourcePackResponse.ResourcePackStackFinished.getValue()); // status
                    wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                }
                case DECLINED -> {
                    wrapper.user().put(new ResourcePacksStorage(wrapper.user()));

                    wrapper.write(Types.BYTE, (byte) ResourcePackResponse.DownloadingFinished.getValue()); // status
                    wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                }
                case ACCEPTED -> {
                    resourcePacksStorage.setJavaClientWaitingForPack(true);
                    final Set<String> missingNonHttpPacks = new HashSet<>();
                    final List<ResourcePack> missingHttpPacks = new ArrayList<>();
                    for (ResourcePack pack : resourcePacksStorage.getPacks()) {
                        if (resourcePacksStorage.isPreloaded(pack.packId())) continue;

                        try {
                            if (Via.getManager().getProviders().get(ResourcePackProvider.class).hasPack(pack)) {
                                Via.getManager().getProviders().get(ResourcePackProvider.class).loadPack(pack);
                            } else if (pack.url() != null) {
                                missingHttpPacks.add(pack);
                            } else {
                                missingNonHttpPacks.add(pack.packId() + "_" + pack.version());
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException("Failed to load resource pack: " + pack.packId(), e);
                        }
                    }

                    if (missingNonHttpPacks.isEmpty() && missingHttpPacks.isEmpty()) {
                        wrapper.write(Types.BYTE, (byte) ResourcePackResponse.DownloadingFinished.getValue()); // status
                        wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, new String[0]); // pack ids
                    } else {
                        if (!missingHttpPacks.isEmpty()) {
                            if (missingNonHttpPacks.isEmpty()) {
                                wrapper.cancel();
                            }
                            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Downloading " + missingHttpPacks.size() + " HTTP packs");
                            resourcePacksStorage.runHttpTask(missingHttpPacks, pack -> {
                                final BedrockPackDownloader downloader = new BedrockPackDownloader(pack.url());
                                final byte[] data = downloader.download();
                                pack.setCompressedDataLength(data.length, data.length);
                                wrapper.user().getChannel().eventLoop().submit(() -> {
                                    try {
                                        if (pack.processDataChunk(0, data)) {
                                            Via.getManager().getProviders().get(ResourcePackProvider.class).addPack(pack);
                                            resourcePacksStorage.sendResponseIfAllDownloadsCompleted();
                                        }
                                    } catch (Throwable e) {
                                        BedrockProtocol.kickForIllegalState(wrapper.user(), "One of the server resource packs failed to process. Please try again later or decline the packs.", e);
                                    }
                                });
                            }, (pack, e) -> BedrockProtocol.kickForIllegalState(wrapper.user(), "One of the server resource packs failed to download. Please try again later or decline the packs.", e));
                        }

                        if (!missingNonHttpPacks.isEmpty()) {
                            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Downloading " + missingNonHttpPacks.size() + " non HTTP packs");
                            wrapper.write(Types.BYTE, (byte) ResourcePackResponse.Downloading.getValue()); // status
                            wrapper.write(BedrockTypes.SHORT_LE_STRING_ARRAY, missingNonHttpPacks.toArray(new String[0])); // pack ids
                        }
                    }
                }
                case DOWNLOADED -> wrapper.cancel();
                default -> throw new IllegalStateException("Unhandled ResourcePackAction: " + status);
            }
        });
    }

}
