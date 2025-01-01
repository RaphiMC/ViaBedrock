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

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.base.ServerboundStatusPackets;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class StatusPackets {

    private static final String NULL_UUID = new UUID(0, 0).toString();

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(State.STATUS, 28 /* UNCONNECTED_PONG */, ClientboundStatusPackets.STATUS_RESPONSE.getId(), wrapper -> {
            final long ping = (System.nanoTime() / 1_000_000) - wrapper.read(Types.LONG); // timestamp
            final String data = new String(wrapper.read(Types.REMAINING_BYTES), StandardCharsets.UTF_8); // data
            final String[] splitData = data.split(";");

            final JsonObject statusResponse = new JsonObject();
            final JsonObject version = new JsonObject();
            final JsonObject players = new JsonObject();
            final JsonArray samples = new JsonArray();
            statusResponse.add("version", version);
            statusResponse.add("players", players);
            players.add("sample", samples);

            final JsonObject pingSample = new JsonObject();
            pingSample.addProperty("name", "Ping: " + ping + "ms");
            pingSample.addProperty("id", NULL_UUID);
            samples.add(pingSample);

            statusResponse.addProperty("description", "");
            version.addProperty("name", "");
            version.addProperty("protocol", wrapper.user().getProtocolInfo().protocolVersion().getVersion());
            players.addProperty("max", -1);
            players.addProperty("online", -1);

            switch (splitData.length) {
                default:
                case 12: { // IPv6 Port
                    final JsonObject sample = new JsonObject();
                    sample.addProperty("name", "IPv6 Port: " + splitData[11]);
                    sample.addProperty("id", NULL_UUID);
                    samples.add(sample);
                }
                case 11: { // IPv4 Port
                    final JsonObject sample = new JsonObject();
                    sample.addProperty("name", "IPv4 Port: " + splitData[10]);
                    sample.addProperty("id", NULL_UUID);
                    samples.add(sample);
                }
                case 10: { // Nintendo limited
                    final JsonObject sample = new JsonObject();
                    sample.addProperty("name", "Nintendo limited: " + !"1".equalsIgnoreCase(splitData[9]));
                    sample.addProperty("id", NULL_UUID);
                    samples.add(sample);
                }
                case 9: { // GameType
                    final JsonObject sample = new JsonObject();
                    sample.addProperty("name", "GameType: " + splitData[8]);
                    sample.addProperty("id", NULL_UUID);
                    samples.add(sample);
                }
                case 8: // Sub MOTD
                case 7: // Server id
                case 6: // Max players
                    try {
                        players.addProperty("max", Integer.parseInt(splitData[5]));
                    } catch (NumberFormatException ignored) {
                    }
                case 5: // Online players
                    try {
                        players.addProperty("online", Integer.parseInt(splitData[4]));
                    } catch (NumberFormatException ignored) {
                    }
                case 4: // Version
                case 3: { // Protocol version
                    final JsonObject sample = new JsonObject();
                    sample.addProperty("name", "Protocol: " + splitData[2]);
                    sample.addProperty("id", NULL_UUID);
                    samples.add(sample);
                }
                case 2: // MOTD
                    statusResponse.addProperty("description", splitData[1] + (splitData.length >= 8 ? ("\n§r" + splitData[7]) : ""));
                case 1: { // Edition
                    final String versionName = splitData[0] + (splitData.length >= 4 ? (" " + splitData[3]) : "");
                    version.addProperty("name", versionName);

                    final JsonObject sample = new JsonObject();
                    sample.addProperty("name", "Version: " + versionName);
                    sample.addProperty("id", NULL_UUID);
                    samples.add(sample);
                }
                case 0:
                    break;
            }

            wrapper.write(Types.STRING, statusResponse.toString()); // status json
        });

        protocol.registerServerbound(State.STATUS, ServerboundStatusPackets.STATUS_REQUEST.getId(), 1 /* UNCONNECTED_PING */, wrapper -> {
            wrapper.write(Types.LONG, System.nanoTime() / 1_000_000); // timestamp (system uptime)
        });
        protocol.registerServerbound(State.STATUS, ServerboundStatusPackets.PING_REQUEST.getId(), -1, wrapper -> {
            wrapper.cancel(); // Ping is added as a part of the player sample instead
            final PacketWrapper pongResponse = PacketWrapper.create(ClientboundStatusPackets.PONG_RESPONSE, wrapper.user());
            pongResponse.write(Types.LONG, wrapper.read(Types.LONG)); // timestamp
            pongResponse.send(BedrockProtocol.class);
        });
    }

}
