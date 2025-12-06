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
package net.raphimc.viabedrock.protocol;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractSimpleProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.FixedByteArrayType;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.base.ServerboundStatusPackets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.netty.nethernet.DiscoveryEncryptionCodec;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.GameType;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

public class NetherNetStatusProtocol extends AbstractSimpleProtocol {

    public static final NetherNetStatusProtocol INSTANCE = new NetherNetStatusProtocol();

    private static final long APPLICATION_ID = 0xDEADBEEFL;
    private static final short ID_DISCOVERY_REQUEST = 0;
    private static final short ID_DISCOVERY_RESPONSE = 1;
    private static final short ID_DISCOVERY_MESSAGE = 2;

    private static final byte[] PADDING_BYTES = new byte[8];
    private static final Type<byte[]> PADDING_TYPE = new FixedByteArrayType(PADDING_BYTES.length);

    private NetherNetStatusProtocol() {
        this.initialize();
    }

    @Override
    protected void registerPackets() {
        this.registerClientbound(State.STATUS, ID_DISCOVERY_RESPONSE, ClientboundStatusPackets.STATUS_RESPONSE.getId(), wrapper -> {
            wrapper.read(Types.BYTE); // The packet id should be a short, so read another byte to make it 2 bytes
            wrapper.user().get(PingInfoStorage.class).serverNetworkId = wrapper.read(BedrockTypes.LONG_LE); // sender network id
            wrapper.read(PADDING_TYPE); // padding
            final String rawData = wrapper.read(BedrockTypes.ASCII_STRING); // data

            final JsonObject statusResponse = new JsonObject();
            final JsonObject version = new JsonObject();
            version.addProperty("name", "");
            version.addProperty("protocol", wrapper.user().getProtocolInfo().protocolVersion().getVersion());
            statusResponse.add("version", version);

            final ByteBuf data = Unpooled.wrappedBuffer(HexFormat.of().parseHex(rawData));
            final int dataVersion = data.readUnsignedByte(); // version
            if (dataVersion != 4) {
                throw new IllegalStateException("Unsupported NetherNet discovery response version: " + dataVersion);
            }

            final String serverName = BedrockTypes.STRING.read(data); // server name
            final String levelName = BedrockTypes.STRING.read(data); // level name
            statusResponse.addProperty("description", "Server: " + serverName + "\n§rLevel: " + levelName);

            final JsonArray samples = new JsonArray();
            samples.add(this.createPlayerSample("GameType: " + GameType.getByValue(data.readUnsignedByte() >> 1, GameType.Undefined).name())); // game type

            final JsonObject players = new JsonObject();
            players.addProperty("online", data.readIntLE()); // player count
            players.addProperty("max", data.readIntLE()); // max player count
            players.add("sample", samples);
            statusResponse.add("players", players);

            samples.add(this.createPlayerSample("Editor World: " + data.readBoolean())); // editor world
            samples.add(this.createPlayerSample("Hardcore: " + data.readBoolean())); // hardcore

            data.skipBytes(2); // unknown
            data.release();

            wrapper.write(Types.STRING, statusResponse.toString()); // status
        });
        this.registerClientbound(State.STATUS, ID_DISCOVERY_MESSAGE, ClientboundStatusPackets.PONG_RESPONSE.getId(), wrapper -> {
            wrapper.read(Types.BYTE); // The packet id should be a short, so read another byte to make it 2 bytes
            wrapper.read(BedrockTypes.LONG_LE); // sender network id
            wrapper.read(PADDING_TYPE); // padding
            wrapper.read(BedrockTypes.LONG_LE); // recipient network id
            final String message = wrapper.read(BedrockTypes.ASCII_STRING); // message
            if (message.equals("Ping")) {
                wrapper.write(Types.LONG, wrapper.user().get(PingInfoStorage.class).pingTime); // time
            } else {
                throw new IllegalStateException("Unsupported NetherNet discovery message: " + message);
            }
        });

        this.registerServerbound(State.STATUS, ServerboundStatusPackets.STATUS_REQUEST.getId(), ID_DISCOVERY_REQUEST, wrapper -> {
            wrapper.write(Types.BYTE, (byte) 0); // The packet id should be a short, so write another byte to make it 2 bytes
            wrapper.write(BedrockTypes.LONG_LE, wrapper.user().get(PingInfoStorage.class).clientNetworkId); // sender network id
            wrapper.write(PADDING_TYPE, PADDING_BYTES); // padding
        });
        this.registerServerbound(State.STATUS, ServerboundStatusPackets.PING_REQUEST.getId(), ID_DISCOVERY_MESSAGE, wrapper -> {
            final PingInfoStorage pingInfoStorage = wrapper.user().get(PingInfoStorage.class);
            pingInfoStorage.pingTime = wrapper.read(Types.LONG); // time

            wrapper.write(Types.BYTE, (byte) 0); // The packet id should be a short, so write another byte to make it 2 bytes
            wrapper.write(BedrockTypes.LONG_LE, pingInfoStorage.clientNetworkId); // sender network id
            wrapper.write(PADDING_TYPE, PADDING_BYTES); // padding
            wrapper.write(BedrockTypes.LONG_LE, pingInfoStorage.serverNetworkId); // recipient network id
            wrapper.write(BedrockTypes.ASCII_STRING, "Ping"); // message
        });
    }

    @Override
    public void init(final UserConnection connection) {
        connection.put(new PingInfoStorage(ThreadLocalRandom.current().nextLong()));

        if (connection.getChannel() != null) {
            try {
                connection.getChannel().pipeline().addBefore(Via.getManager().getInjector().getEncoderName(), "viabedrock-nethernet-discovery-encryption", new DiscoveryEncryptionCodec(APPLICATION_ID));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to initialize NetherNet encryption codec", e);
            }
        }
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

    private static class PingInfoStorage implements StorableObject {

        private final long clientNetworkId;
        private long serverNetworkId;
        private long pingTime;

        private PingInfoStorage(final long clientNetworkId) {
            this.clientNetworkId = clientNetworkId;
        }

    }

}
