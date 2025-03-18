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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.util.Key;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.api.modinterface.ViaBedrockUtilityInterface;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.Connection_DisconnectFailReason;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.MinecraftPacketIds;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PacketViolationSeverity;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PacketViolationType;
import net.raphimc.viabedrock.protocol.storage.ChannelStorage;
import net.raphimc.viabedrock.protocol.storage.ClientSettingsStorage;
import net.raphimc.viabedrock.protocol.storage.PacketSyncStorage;
import net.raphimc.viabedrock.protocol.task.KeepAliveTask;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultiStatePackets {

    private static final PacketHandler DISCONNECT_HANDLER = wrapper -> {
        final Connection_DisconnectFailReason disconnectReason = Connection_DisconnectFailReason.getByValue(wrapper.read(BedrockTypes.VAR_INT), Connection_DisconnectFailReason.Unknown); // reason
        final boolean hasMessage = !wrapper.read(Types.BOOLEAN); // skip message
        if (hasMessage) {
            final Map<String, String> translations = BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePacks().get("vanilla").content().getLang("texts/en_US.lang");
            final Function<String, String> translator = k -> translations.getOrDefault(k, k);
            final String rawMessage = wrapper.read(BedrockTypes.STRING); // message
            wrapper.read(BedrockTypes.STRING); // filtered message
            final String translatedMessage = BedrockTranslator.translate(rawMessage, translator, new Object[0]);
            PacketFactory.writeJavaDisconnect(wrapper, translatedMessage + " §r(Reason: " + disconnectReason + ")");
        } else {
            PacketFactory.writeJavaDisconnect(wrapper, null);
        }
    };

    private static final PacketHandler PACKET_VIOLATION_WARNING_HANDLER = wrapper -> {
        final PacketViolationType type = PacketViolationType.getByValue(wrapper.read(BedrockTypes.VAR_INT), PacketViolationType.Unknown); // type
        final PacketViolationSeverity severity = PacketViolationSeverity.getByValue(wrapper.read(BedrockTypes.VAR_INT), PacketViolationSeverity.Unknown); // severity
        final int packetIdCause = wrapper.read(BedrockTypes.VAR_INT); // cause packet id
        final String context = wrapper.read(BedrockTypes.STRING); // context

        final MinecraftPacketIds packet = MinecraftPacketIds.getByValue(packetIdCause);

        final String reason = "§4Packet violation warning: §c"
                + type.name()
                + " (" + severity.name() + ")\n"
                + "Violating Packet: " + (packet != null ? packet.name() : packetIdCause) + "\n"
                + (context.isEmpty() ? "No context provided" : (" Context: '" + context + "'"))
                + "\n\nPlease report this issue on the ViaBedrock GitHub page!";
        PacketFactory.writeJavaDisconnect(wrapper, reason);
    };

    private static final PacketHandlers KEEP_ALIVE_HANDLER = new PacketHandlers() {
        @Override
        public void register() {
            map(Types.LONG, BedrockTypes.LONG_LE); // id
            create(Types.BOOLEAN, true); // from server
            handler(wrapper -> {
                if (wrapper.get(BedrockTypes.LONG_LE, 0) == KeepAliveTask.INTERNAL_ID) { // It's a keep alive packet sent from ViaBedrock to prevent the client from disconnecting
                    wrapper.cancel();
                }
            });
        }
    };

    private static final PacketHandlers NETWORK_STACK_LATENCY_HANDLER = new PacketHandlers() {
        @Override
        protected void register() {
            map(BedrockTypes.LONG_LE, Types.LONG, t -> t * 1_000_000); // timestamp
            handler(wrapper -> {
                if (!wrapper.read(Types.BOOLEAN)) { // from server
                    wrapper.cancel();
                }
            });
        }
    };

    public static final PacketHandler CLIENT_SETTINGS_HANDLER = wrapper -> {
        final String locale = wrapper.read(Types.STRING); // locale
        final byte viewDistance = wrapper.read(Types.BYTE); // view distance
        final int chatVisibility = wrapper.read(Types.VAR_INT); // chat visibility
        final boolean chatColors = wrapper.read(Types.BOOLEAN); // chat colors
        final short skinParts = wrapper.read(Types.UNSIGNED_BYTE); // skin parts
        final int mainHand = wrapper.read(Types.VAR_INT); // main hand
        final boolean textFiltering = wrapper.read(Types.BOOLEAN); // text filtering
        final boolean allowsListing = wrapper.read(Types.BOOLEAN); // allows listing
        final int particleStatus = wrapper.read(Types.VAR_INT); // particle status
        wrapper.user().put(new ClientSettingsStorage(locale, viewDistance, chatVisibility, chatColors, skinParts, mainHand, textFiltering, allowsListing, particleStatus));

        wrapper.write(BedrockTypes.VAR_INT, (int) viewDistance); // radius
        wrapper.write(Types.BYTE, ProtocolConstants.BEDROCK_REQUEST_CHUNK_RADIUS_MAX_RADIUS); // max radius
    };

    public static final PacketHandler CUSTOM_PAYLOAD_HANDLER = wrapper -> {
        wrapper.cancel();
        final String channel = Key.namespaced(wrapper.read(Types.STRING)); // channel

        if (channel.equals("minecraft:register")) {
            final List<String> channels = Arrays.asList(new String(wrapper.read(Types.SERVERBOUND_CUSTOM_PAYLOAD_DATA), StandardCharsets.UTF_8).split("\0"));

            if (channels.contains(ViaBedrockUtilityInterface.CONFIRM_CHANNEL)) {
                ViaBedrockUtilityInterface.confirm(wrapper.user());
            }

            wrapper.user().get(ChannelStorage.class).addChannels(channels);
        }
    };

    public static final PacketHandler PONG_HANDLER = wrapper -> {
        wrapper.cancel();
        wrapper.user().get(PacketSyncStorage.class).handleResponse(wrapper.read(Types.INT)); // parameter
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientboundTransition(ClientboundBedrockPackets.DISCONNECT,
                ClientboundPackets1_21_2.DISCONNECT, DISCONNECT_HANDLER,
                ClientboundLoginPackets.LOGIN_DISCONNECT, DISCONNECT_HANDLER,
                ClientboundConfigurationPackets1_21.DISCONNECT, DISCONNECT_HANDLER
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.PACKET_VIOLATION_WARNING,
                ClientboundPackets1_21_2.DISCONNECT, PACKET_VIOLATION_WARNING_HANDLER,
                ClientboundLoginPackets.LOGIN_DISCONNECT, PACKET_VIOLATION_WARNING_HANDLER,
                ClientboundConfigurationPackets1_21.DISCONNECT, PACKET_VIOLATION_WARNING_HANDLER
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.NETWORK_STACK_LATENCY,
                ClientboundPackets1_21_2.KEEP_ALIVE, NETWORK_STACK_LATENCY_HANDLER,
                State.LOGIN, (PacketHandler) wrapper -> {
                    NETWORK_STACK_LATENCY_HANDLER.handle(wrapper);
                    if (!wrapper.isCancelled()) {
                        wrapper.resetReader();
                        KEEP_ALIVE_HANDLER.handle(wrapper);
                        if (!wrapper.isCancelled()) {
                            wrapper.setPacketType(ServerboundBedrockPackets.NETWORK_STACK_LATENCY);
                            wrapper.sendToServer(BedrockProtocol.class);
                            wrapper.cancel();
                        }
                    }
                },
                ClientboundConfigurationPackets1_21.KEEP_ALIVE, NETWORK_STACK_LATENCY_HANDLER
        );

        protocol.registerServerbound(ServerboundPackets1_21_4.KEEP_ALIVE, ServerboundBedrockPackets.NETWORK_STACK_LATENCY, KEEP_ALIVE_HANDLER);
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_5.KEEP_ALIVE, ServerboundBedrockPackets.NETWORK_STACK_LATENCY, KEEP_ALIVE_HANDLER);
    }

}
