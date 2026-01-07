/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.FixedByteArrayType;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.base.ServerboundStatusPackets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class RakNetStatusProtocol extends AbstractSimpleProtocol {

    public static final RakNetStatusProtocol INSTANCE = new RakNetStatusProtocol();

    private static final byte[] OFFLINE_MESSAGE_DATA_ID = {0x00, (byte) 0xFF, (byte) 0xFF, 0x00, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, 0x12, 0x34, 0x56, 0x78};
    private static final byte ID_UNCONNECTED_PING = 1;
    private static final byte ID_UNCONNECTED_PONG = 28;

    private static final Type<byte[]> OFFLINE_MESSAGE_DATA_ID_TYPE = new FixedByteArrayType(OFFLINE_MESSAGE_DATA_ID.length);

    private RakNetStatusProtocol() {
        this.initialize();
    }

    @Override
    protected void registerPackets() {
        this.registerClientbound(State.STATUS, ID_UNCONNECTED_PONG, ClientboundStatusPackets.STATUS_RESPONSE.getId(), wrapper -> {
            final long ping = (System.nanoTime() - wrapper.user().get(PingInfoStorage.class).pingStartTime) / 1_000_000L;
            wrapper.read(Types.LONG); // time (system uptime)
            wrapper.read(Types.LONG); // guid
            final byte[] offlineMessageDataId = wrapper.read(OFFLINE_MESSAGE_DATA_ID_TYPE); // offline message data id
            if (!Arrays.equals(offlineMessageDataId, OFFLINE_MESSAGE_DATA_ID)) {
                throw new IllegalStateException("Invalid offline message data id");
            }
            final byte[] data = wrapper.read(Types.SHORT_BYTE_ARRAY); // ping response data

            final JsonObject statusResponse = new JsonObject();
            statusResponse.addProperty("description", "");

            final JsonObject version = new JsonObject();
            version.addProperty("name", "");
            version.addProperty("protocol", wrapper.user().getProtocolInfo().protocolVersion().getVersion());
            statusResponse.add("version", version);

            final JsonArray samples = new JsonArray();
            samples.add(this.createPlayerSample("Ping: " + ping + "ms"));

            final JsonObject players = new JsonObject();
            players.addProperty("online", -1);
            players.addProperty("max", -1);
            players.add("sample", samples);
            statusResponse.add("players", players);

            final String[] splitData = new String(data, StandardCharsets.UTF_8).split(";");
            switch (splitData.length) {
                default:
                case 12: // IPv6 Port
                    samples.add(this.createPlayerSample("IPv6 Port: " + splitData[11]));
                case 11: // IPv4 Port
                    samples.add(this.createPlayerSample("IPv4 Port: " + splitData[10]));
                case 10: // Nintendo limited
                    samples.add(this.createPlayerSample("Nintendo limited: " + !"1".equalsIgnoreCase(splitData[9])));
                case 9: // GameType
                    samples.add(this.createPlayerSample("GameType: " + splitData[8]));
                case 8: // Sub MOTD
                case 7: // Server unique id
                case 6: // Max player count
                    try {
                        players.addProperty("max", Integer.parseInt(splitData[5]));
                    } catch (NumberFormatException ignored) {
                    }
                case 5: // Player count
                    try {
                        players.addProperty("online", Integer.parseInt(splitData[4]));
                    } catch (NumberFormatException ignored) {
                    }
                case 4: // Version name
                case 3: // Protocol version
                    samples.add(this.createPlayerSample("Protocol: " + splitData[2]));
                case 2: // MOTD
                    statusResponse.addProperty("description", splitData[1] + (splitData.length >= 8 ? ("\n§r" + splitData[7]) : ""));
                case 1: { // Edition
                    final String versionName = splitData[0] + (splitData.length >= 4 ? (" " + splitData[3]) : "");
                    version.addProperty("name", versionName);
                    samples.add(this.createPlayerSample("Version: " + versionName));
                }
                case 0:
            }

            wrapper.write(Types.STRING, statusResponse.toString()); // status
        });

        this.registerServerbound(State.STATUS, ServerboundStatusPackets.STATUS_REQUEST.getId(), ID_UNCONNECTED_PING, wrapper -> {
            wrapper.write(Types.LONG, System.nanoTime() / 1_000_000L); // time (system uptime)
            wrapper.write(OFFLINE_MESSAGE_DATA_ID_TYPE, OFFLINE_MESSAGE_DATA_ID); // offline message data id
            wrapper.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // guid

            wrapper.user().put(new PingInfoStorage(System.nanoTime()));
        });
        this.registerServerbound(State.STATUS, ServerboundStatusPackets.PING_REQUEST.getId(), -1, wrapper -> {
            wrapper.cancel(); // Ping is added as a part of the player sample instead
            final PacketWrapper pongResponse = PacketWrapper.create(ClientboundStatusPackets.PONG_RESPONSE, wrapper.user());
            pongResponse.write(Types.LONG, wrapper.read(Types.LONG)); // time
            pongResponse.send(RakNetStatusProtocol.class);
        });
    }

    @Override
    public boolean isBaseProtocol() {
        return true;
    }

    private JsonObject createPlayerSample(final String text) {
        final JsonObject sample = new JsonObject();
        sample.addProperty("name", text);
        sample.addProperty("id", "00000000-0000-0000-0000-000000000000");
        return sample;
    }

    private record PingInfoStorage(long pingStartTime) implements StorableObject {
    }

}
