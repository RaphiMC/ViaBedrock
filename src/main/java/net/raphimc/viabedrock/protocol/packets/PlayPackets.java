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
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InteractAction;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MovePlayerMode;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayStatus;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;

public class PlayPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.PLAY_STATUS, ClientboundPackets1_19_3.DISCONNECT, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final int status = wrapper.read(Type.INT); // status
                    final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                    final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);

                    if (status == PlayStatus.LOGIN_SUCCESS) {
                        wrapper.cancel();
                        final PacketWrapper clientCacheStatus = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_CACHE_STATUS, wrapper.user());
                        clientCacheStatus.write(Type.BOOLEAN, false); // is supported
                        clientCacheStatus.sendToServer(BedrockProtocol.class);
                    }
                    if (status == PlayStatus.PLAYER_SPAWN) { // First spawn
                        wrapper.cancel();
                        final ClientPlayerEntity clientPlayer = entityTracker.getClientPlayer();
                        if (clientPlayer.isInitiallySpawned()) {
                            return; // Mojang client silently ignores this packet if the player is already spawned
                        }
                        if (gameSession.getBedrockBiomeDefinitions() == null) {
                            BedrockProtocol.kickForIllegalState(wrapper.user(), "Tried to spawn the client player before the biome definitions were loaded!");
                            return;
                        }

                        final PacketWrapper interact = PacketWrapper.create(ServerboundBedrockPackets.INTERACT, wrapper.user());
                        interact.write(Type.UNSIGNED_BYTE, InteractAction.MOUSEOVER); // action
                        interact.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                        interact.write(BedrockTypes.POSITION_3F, new Position3f(0F, 0F, 0F)); // mouse position
                        interact.sendToServer(BedrockProtocol.class);

                        // TODO: Mob Equipment with current held item

                        final PacketWrapper emoteList = PacketWrapper.create(ServerboundBedrockPackets.EMOTE_LIST, wrapper.user());
                        emoteList.write(BedrockTypes.VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                        emoteList.write(BedrockTypes.UUID_ARRAY, new UUID[0]); // emote ids
                        emoteList.sendToServer(BedrockProtocol.class);

                        clientPlayer.setRotation(new Position3f(clientPlayer.rotation().x(), clientPlayer.rotation().y(), clientPlayer.rotation().y()));
                        clientPlayer.setInitiallySpawned(true);
                        clientPlayer.sendMovementPacketToServer(wrapper.user(), MovePlayerMode.NORMAL);

                        final PacketWrapper setLocalPlayerAsInitialized = PacketWrapper.create(ServerboundBedrockPackets.SET_LOCAL_PLAYER_AS_INITIALIZED, wrapper.user());
                        setLocalPlayerAsInitialized.write(BedrockTypes.UNSIGNED_VAR_LONG, clientPlayer.runtimeId()); // runtime entity id
                        setLocalPlayerAsInitialized.sendToServer(BedrockProtocol.class);

                        clientPlayer.closeDownloadingTerrainScreen(wrapper.user());
                    } else {
                        LoginPackets.writePlayStatusKickMessage(wrapper, status);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SET_DIFFICULTY, ClientboundPackets1_19_3.SERVER_DIFFICULTY, new PacketHandlers() {
            @Override
            public void register() {
                map(BedrockTypes.UNSIGNED_VAR_INT, Type.UNSIGNED_BYTE); // difficulty
                create(Type.BOOLEAN, false); // locked
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.NETWORK_STACK_LATENCY, ClientboundPackets1_19_3.KEEP_ALIVE, new PacketHandlers() {
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

        protocol.registerServerbound(ServerboundPackets1_19_3.CLIENT_SETTINGS, ServerboundBedrockPackets.REQUEST_CHUNK_RADIUS, new PacketHandlers() {
            @Override
            public void register() {
                read(Type.STRING); // locale
                map(Type.BYTE, BedrockTypes.VAR_INT); // view distance
                read(Type.VAR_INT); // chat visibility
                read(Type.BOOLEAN); // chat colors
                read(Type.UNSIGNED_BYTE); // skin parts
                read(Type.VAR_INT); // main hand
                read(Type.BOOLEAN); // text filtering
                read(Type.BOOLEAN); // server listing
            }
        });
        protocol.registerServerbound(ServerboundPackets1_19_3.KEEP_ALIVE, ServerboundBedrockPackets.NETWORK_STACK_LATENCY, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.LONG, BedrockTypes.LONG_LE); // id
                create(Type.BOOLEAN, true); // from server
            }
        });
    }

}
