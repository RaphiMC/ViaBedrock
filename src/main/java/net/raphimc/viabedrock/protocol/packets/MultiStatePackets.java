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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import net.lenni0451.mcstructs_bedrock.text.utils.BedrockTranslator;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.DisconnectReason;
import net.raphimc.viabedrock.protocol.storage.ChannelStorage;
import net.raphimc.viabedrock.protocol.storage.ClientSettingsStorage;
import net.raphimc.viabedrock.protocol.storage.PacketSyncStorage;
import net.raphimc.viabedrock.protocol.task.KeepAliveTask;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class MultiStatePackets {

    private static final PacketHandler DISCONNECT_HANDLER = wrapper -> {
        final DisconnectReason disconnectReason = DisconnectReason.fromId(wrapper.read(BedrockTypes.VAR_INT)); // reason
        final boolean hasMessage = !wrapper.read(Type.BOOLEAN); // skip message
        if (hasMessage) {
            final Map<String, String> translations = BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePack().content().getLang("texts/en_US.lang");
            final Function<String, String> translator = k -> translations.getOrDefault(k, k);
            final String rawMessage = wrapper.read(BedrockTypes.STRING); // message
            final String translatedMessage = BedrockTranslator.translate(rawMessage, translator, new Object[0]);
            PacketFactory.writeDisconnect(wrapper, translatedMessage + " §r(Reason: " + disconnectReason + ")");
        } else {
            PacketFactory.writeDisconnect(wrapper, null);
        }
    };

    private static final PacketHandler PACKET_VIOLATION_WARNING_HANDLER = wrapper -> {
        final int type = wrapper.read(BedrockTypes.VAR_INT) + 1; // type
        final int severity = wrapper.read(BedrockTypes.VAR_INT) + 1; // severity
        final int packetIdCause = wrapper.read(BedrockTypes.VAR_INT); // cause packet id
        final String context = wrapper.read(BedrockTypes.STRING); // context

        final String[] types = new String[]{"Unknown", "Malformed packet"};
        final String[] severities = new String[]{"Unknown", "Warning", "Final warning", "Terminating connection"};

        final String reason = "§4Packet violation warning: §c"
                + (type >= 0 && type <= types.length ? types[type] : type)
                + " (" + (severity >= 0 && severity <= severities.length ? severities[severity] : severity) + ")\n"
                + "Violating Packet: " + (ServerboundBedrockPackets.getPacket(packetIdCause) != null ? ServerboundBedrockPackets.getPacket(packetIdCause).name() : packetIdCause) + "\n"
                + (context.isEmpty() ? "No context provided" : (" Context: '" + context + "'"))
                + "\n\nPlease report this issue on the ViaBedrock GitHub page!";
        PacketFactory.writeDisconnect(wrapper, reason);
    };

    private static final PacketHandlers KEEP_ALIVE_HANDLER = new PacketHandlers() {
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
    };

    private static final PacketHandlers NETWORK_STACK_LATENCY_HANDLER = new PacketHandlers() {
        @Override
        protected void register() {
            map(BedrockTypes.LONG_LE, Type.LONG, t -> t * 1_000_000); // timestamp
            handler(wrapper -> {
                if (!wrapper.read(Type.BOOLEAN)) { // from server
                    wrapper.cancel();
                }
            });
        }
    };

    public static final PacketHandler CLIENT_SETTINGS_HANDLER = wrapper -> {
        final String locale = wrapper.read(Type.STRING); // locale
        final byte viewDistance = wrapper.read(Type.BYTE); // view distance
        final int chatVisibility = wrapper.read(Type.VAR_INT); // chat visibility
        final boolean chatColors = wrapper.read(Type.BOOLEAN); // chat colors
        final short skinParts = wrapper.read(Type.UNSIGNED_BYTE); // skin parts
        final int mainHand = wrapper.read(Type.VAR_INT); // main hand
        final boolean textFiltering = wrapper.read(Type.BOOLEAN); // text filtering
        final boolean serverListing = wrapper.read(Type.BOOLEAN); // server listing
        wrapper.user().put(new ClientSettingsStorage(locale, viewDistance, chatVisibility, chatColors, skinParts, mainHand, textFiltering, serverListing));

        wrapper.write(BedrockTypes.VAR_INT, (int) viewDistance); // radius
        wrapper.write(Type.UNSIGNED_BYTE, ProtocolConstants.BEDROCK_REQUEST_CHUNK_RADIUS_MAX_RADIUS); // max radius
    };

    public static final PacketHandler CUSTOM_PAYLOAD_HANDLER = wrapper -> {
        wrapper.cancel();
        final String channel = wrapper.read(Type.STRING); // channel
        if (channel.equals("minecraft:register")) {
            final String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
            wrapper.user().get(ChannelStorage.class).addChannels(channels);
        }
    };

    public static final PacketHandler PONG_HANDLER = wrapper -> {
        wrapper.cancel();
        wrapper.user().get(PacketSyncStorage.class).handleResponse(wrapper.read(Type.INT)); // parameter
    };

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientboundTransition(ClientboundBedrockPackets.DISCONNECT,
                ClientboundPackets1_20_3.DISCONNECT, DISCONNECT_HANDLER,
                ClientboundLoginPackets.LOGIN_DISCONNECT, DISCONNECT_HANDLER,
                ClientboundConfigurationPackets1_20_3.DISCONNECT, DISCONNECT_HANDLER
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.PACKET_VIOLATION_WARNING,
                ClientboundPackets1_20_3.DISCONNECT, PACKET_VIOLATION_WARNING_HANDLER,
                ClientboundLoginPackets.LOGIN_DISCONNECT, PACKET_VIOLATION_WARNING_HANDLER,
                ClientboundConfigurationPackets1_20_3.DISCONNECT, PACKET_VIOLATION_WARNING_HANDLER
        );
        protocol.registerClientboundTransition(ClientboundBedrockPackets.NETWORK_STACK_LATENCY,
                ClientboundPackets1_20_3.KEEP_ALIVE, NETWORK_STACK_LATENCY_HANDLER,
                ClientboundConfigurationPackets1_20_3.KEEP_ALIVE, NETWORK_STACK_LATENCY_HANDLER
        );

        protocol.registerServerbound(ServerboundPackets1_20_3.KEEP_ALIVE, ServerboundBedrockPackets.NETWORK_STACK_LATENCY, KEEP_ALIVE_HANDLER);
        protocol.registerServerboundTransition(ServerboundConfigurationPackets1_20_2.KEEP_ALIVE, ServerboundBedrockPackets.NETWORK_STACK_LATENCY, KEEP_ALIVE_HANDLER);
    }

}
