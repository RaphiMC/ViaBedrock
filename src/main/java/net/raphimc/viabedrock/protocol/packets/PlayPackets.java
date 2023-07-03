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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InteractActions;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MovePlayerModes;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayStatus;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ServerMovementModes;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.providers.TransferProvider;
import net.raphimc.viabedrock.protocol.storage.*;
import net.raphimc.viabedrock.protocol.task.KeepAliveTask;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PlayPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.DISCONNECT, ClientboundPackets1_19_4.DISCONNECT, wrapper -> {
            final boolean hasMessage = !wrapper.read(Type.BOOLEAN); // skip message
            if (hasMessage) {
                final Map<String, String> translations = BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePack().content().getLang("texts/en_US.lang");
                final Function<String, String> translator = k -> translations.getOrDefault(k, k);
                final String rawMessage = wrapper.read(BedrockTypes.STRING); // message
                final String translatedMessage = BedrockTranslator.translate(rawMessage, translator, new Object[0]);
                wrapper.write(Type.COMPONENT, TextUtil.stringToGson(translatedMessage)); // reason
            } else {
                wrapper.write(Type.COMPONENT, com.viaversion.viaversion.libs.gson.JsonNull.INSTANCE); // reason
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.PLAY_STATUS, ClientboundPackets1_19_4.DISCONNECT, wrapper -> {
            final int status = wrapper.read(Type.INT); // status
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

            if (status == PlayStatus.LOGIN_SUCCESS) {
                wrapper.cancel();
                final PacketWrapper clientCacheStatus = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_CACHE_STATUS, wrapper.user());
                clientCacheStatus.write(Type.BOOLEAN, ViaBedrock.getConfig().isBlobCacheEnabled()); // is supported
                clientCacheStatus.sendToServer(BedrockProtocol.class);
            }
            if (status == PlayStatus.PLAYER_SPAWN) { // Spawn player
                wrapper.cancel();
                final ClientPlayerEntity clientPlayer = entityTracker.getClientPlayer();
                if (clientPlayer.isInitiallySpawned()) {
                    if (clientPlayer.isChangingDimension()) {
                        clientPlayer.closeDownloadingTerrainScreen();
                    }

                    return;
                }
                if (gameSession.getBedrockBiomeDefinitions() == null) {
                    BedrockProtocol.kickForIllegalState(wrapper.user(), "Tried to spawn the client player before the biome definitions were loaded!");
                    return;
                }

                final PacketWrapper interact = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, wrapper.user());
                interact.write(Type.UNSIGNED_BYTE, InteractActions.MOUSEOVER); // action
                interact.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                interact.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // mouse position
                interact.sendToServer(BedrockProtocol.class);

                // TODO: Mob Equipment with current held item

                final PacketWrapper emoteList = PacketWrapper.create(ServerboundBedrockPackets.EMOTE_LIST, wrapper.user());
                emoteList.write(BedrockTypes.VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                emoteList.write(BedrockTypes.UUID_ARRAY, new UUID[0]); // emote ids
                emoteList.sendToServer(BedrockProtocol.class);

                clientPlayer.setRotation(new Position3f(clientPlayer.rotation().x(), clientPlayer.rotation().y(), clientPlayer.rotation().y()));
                clientPlayer.setInitiallySpawned();
                if (gameSession.getMovementMode() == ServerMovementModes.CLIENT) {
                    clientPlayer.sendMovePlayerPacketToServer(MovePlayerModes.NORMAL);
                }

                final PacketWrapper setLocalPlayerAsInitialized = PacketWrapper.create(ServerboundBedrockPackets.SET_LOCAL_PLAYER_AS_INITIALIZED, wrapper.user());
                setLocalPlayerAsInitialized.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                setLocalPlayerAsInitialized.sendToServer(BedrockProtocol.class);

                clientPlayer.closeDownloadingTerrainScreen();

                if (wrapper.user().getProtocolInfo().getProtocolVersion() >= ProtocolVersion.v1_20.getVersion()) {
                    final UUID httpToken = UUID.randomUUID();
                    ViaBedrock.getResourcePackServer().addConnection(httpToken, wrapper.user());

                    final PacketWrapper resourcePack = PacketWrapper.create(ClientboundPackets1_19_4.RESOURCE_PACK, wrapper.user());
                    resourcePack.write(Type.STRING, ViaBedrock.getResourcePackServer().getUrl() + "?token=" + httpToken); // url
                    resourcePack.write(Type.STRING, ""); // hash
                    resourcePack.write(Type.BOOLEAN, false); // requires accept
                    resourcePack.write(Type.OPTIONAL_COMPONENT, TextUtil.stringToGson("\nFor the best possible experience, you should accept the resource pack.")); // prompt message
                    resourcePack.send(BedrockProtocol.class);
                }
            } else {
                LoginPackets.writePlayStatusKickMessage(wrapper, status);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DIFFICULTY, ClientboundPackets1_19_4.SERVER_DIFFICULTY, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.UNSIGNED_VAR_INT, Type.UNSIGNED_BYTE); // difficulty
                create(Type.BOOLEAN, false); // locked
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.NETWORK_STACK_LATENCY, ClientboundPackets1_19_4.KEEP_ALIVE, new PacketHandlers() {
            @Override
            protected void register() {
                map(BedrockTypes.LONG_LE, Type.LONG, t -> {
                    if (t >= 0) {
                        return t / 1000 * 1000;
                    } else {
                        final long result = (t - 383) / 1000 * 1000;
                        return result > 0 ? result : result - 616;
                    }
                }); // timestamp
                handler(wrapper -> {
                    if (!wrapper.read(Type.BOOLEAN)) { // from server
                        wrapper.cancel();
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.CLIENT_CACHE_MISS_RESPONSE, null, wrapper -> {
            wrapper.cancel();
            final int length = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // blob count
            for (int i = 0; i < length; i++) {
                final long hash = wrapper.read(BedrockTypes.LONG_LE); // blob hash
                final byte[] blob = wrapper.read(BedrockTypes.BYTE_ARRAY); // blob data
                wrapper.user().get(BlobCache.class).addBlob(hash, blob);
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.TRANSFER, null, wrapper -> {
            wrapper.cancel();
            final String hostname = wrapper.read(BedrockTypes.STRING); // hostname
            final short port = wrapper.read(BedrockTypes.SHORT_LE); // port

            Via.getManager().getProviders().get(TransferProvider.class).connectToServer(wrapper.user(), new InetSocketAddress(hostname, port));
        });

        protocol.registerServerbound(ServerboundPackets1_19_4.CLIENT_SETTINGS, ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, new PacketHandlers() {
            @Override
            public void register() {
                read(Type.STRING); // locale
                map(Type.BYTE, BedrockTypes.VAR_INT); // view distance
                create(Type.UNSIGNED_BYTE, ProtocolConstants.REQUEST_CHUNK_RADIUS_MAX_RADIUS); // max radius
                read(Type.VAR_INT); // chat visibility
                read(Type.BOOLEAN); // chat colors
                read(Type.UNSIGNED_BYTE); // skin parts
                read(Type.VAR_INT); // main hand
                read(Type.BOOLEAN); // text filtering
                read(Type.BOOLEAN); // server listing
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.KEEP_ALIVE, ServerboundBedrockPackets.NETWORK_STACK_LATENCY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.LONG, BedrockTypes.LONG_LE); // id
                create(Type.BOOLEAN, true); // from server
                handler(wrapper -> {
                    if (wrapper.get(BedrockTypes.LONG_LE, 0) == KeepAliveTask.INTERNAL_ID) { // It's a keep alive packet sent from ViaBedrock to prevent the client from disconnecting
                        wrapper.cancel();
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.PONG, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().get(PacketSyncStorage.class).handleResponse(wrapper.read(Type.INT)); // parameter
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.PLUGIN_MESSAGE, null, wrapper -> {
            wrapper.cancel();
            final String channel = wrapper.read(Type.STRING); // channel
            if (channel.equals("minecraft:register")) {
                final String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                wrapper.user().get(ChannelStorage.class).addChannels(channels);
            }
        });
    }

}
