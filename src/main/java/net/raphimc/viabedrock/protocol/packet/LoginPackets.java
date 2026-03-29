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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundHandshakePackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.*;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AuthenticationType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PacketCompressionAlgorithm;
import net.raphimc.viabedrock.protocol.provider.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.provider.SkinProvider;
import net.raphimc.viabedrock.protocol.storage.AuthData;
import net.raphimc.viabedrock.protocol.storage.HandshakeStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class LoginPackets {

    private static final int CLOCK_SKEW = 60;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.NETWORK_SETTINGS, null, wrapper -> {
            wrapper.cancel();
            final HandshakeStorage handshakeStorage = wrapper.user().get(HandshakeStorage.class);
            final AuthData authData = wrapper.user().get(AuthData.class);

            final int threshold = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // compression threshold
            final PacketCompressionAlgorithm algorithm = PacketCompressionAlgorithm.getByValue(wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE), PacketCompressionAlgorithm.None); // compression algorithm
            Via.getManager().getProviders().get(NettyPipelineProvider.class).enableCompression(wrapper.user(), algorithm, threshold);

            boolean isSelfSigned;
            try {
                Jwts.parser().clockSkewSeconds(CLOCK_SKEW).verifyWith(authData.getSessionKeyPair().getPublic()).build().parseSignedClaims(authData.getMultiplayerToken());
                isSelfSigned = true;
            } catch (JwtException e) {
                isSelfSigned = false;
            }

            final JsonObject authInfoObj = new JsonObject();
            authInfoObj.addProperty("AuthenticationType", (!isSelfSigned ? AuthenticationType.Full : AuthenticationType.SelfSigned).ordinal());
            authInfoObj.addProperty("Certificate", "{\"chain\":[\"..\"]}\n");
            authInfoObj.addProperty("Token", authData.getMultiplayerToken());
            final String authInfo = authInfoObj.toString();

            final PacketWrapper login = PacketWrapper.create(ServerboundBedrockPackets.LOGIN, wrapper.user());
            login.write(Types.INT, handshakeStorage.protocolVersion()); // protocol version
            login.write(BedrockTypes.UNSIGNED_VAR_INT, authInfo.length() + authData.getSkinJwt().length() + Integer.BYTES * 2); // length
            login.write(BedrockTypes.ASCII_STRING, authInfo); // auth info
            login.write(BedrockTypes.ASCII_STRING, authData.getSkinJwt()); // client properties
            login.sendToServer(BedrockProtocol.class);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SERVER_TO_CLIENT_HANDSHAKE, null, wrapper -> {
            wrapper.cancel();
            final KeyPair sessionKeyPair = wrapper.user().get(AuthData.class).getSessionKeyPair();
            final Jws<Claims> jwt = Jwts.parser().clockSkewSeconds(CLOCK_SKEW).keyLocator(CryptUtil.X5U_KEY_LOCATOR).build().parseSignedClaims(wrapper.read(BedrockTypes.STRING)); // jwt
            final byte[] salt = Base64.getDecoder().decode(jwt.getPayload().get("salt", String.class));
            final SecretKey secretKey = ecdhKeyExchange(sessionKeyPair.getPrivate(), CryptUtil.X5U_KEY_LOCATOR.locate(jwt.getHeader()), salt);
            Via.getManager().getProviders().get(NettyPipelineProvider.class).enableEncryption(wrapper.user(), secretKey);

            final PacketWrapper clientToServerHandshake = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_TO_SERVER_HANDSHAKE, wrapper.user());
            clientToServerHandshake.sendToServer(BedrockProtocol.class);
        });

        protocol.registerServerboundTransition(ServerboundHandshakePackets.CLIENT_INTENTION, null, wrapper -> {
            wrapper.cancel();
            final int protocolVersion = wrapper.read(Types.VAR_INT); // protocol version
            final String hostname = wrapper.read(Types.STRING); // hostname
            final int port = wrapper.read(Types.UNSIGNED_SHORT); // port
            wrapper.user().put(new HandshakeStorage(protocolVersion, hostname, port));
        });
        protocol.registerServerboundTransition(ServerboundLoginPackets.HELLO, ServerboundBedrockPackets.REQUEST_NETWORK_SETTINGS, wrapper -> {
            final HandshakeStorage handshakeStorage = wrapper.user().get(HandshakeStorage.class);

            if (!ViaBedrock.getConfig().shouldDisableServerBlacklist() && ServerBlacklist.isBlacklisted(handshakeStorage.hostname())) {
                wrapper.cancel();
                try {
                    final PacketWrapper loginDisconnect = PacketWrapper.create(ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper.user());
                    PacketFactory.writeJavaDisconnect(loginDisconnect, "§cThis server is blacklisted by ViaBedrock because the server is known to ban players joining with ViaBedrock (Due to the server's anti-cheat).\n\n§7If you want to join the server anyway, set disable-server-blacklist to true in the ViaBedrock config file.");
                    loginDisconnect.send(BedrockProtocol.class);
                } catch (Throwable ignored) {
                }
                if (wrapper.user().getChannel() != null) {
                    wrapper.user().getChannel().flush();
                    wrapper.user().getChannel().close();
                }
                return;
            }

            final String username = wrapper.read(Types.STRING); // username
            wrapper.read(Types.UUID); // uuid
            wrapper.write(Types.INT, handshakeStorage.protocolVersion()); // protocol version

            if (!wrapper.user().has(AuthData.class)) { // Generate self-signed JWT if no AuthData has been provided by the implementation
                final Instant now = Instant.now();
                final KeyPair sessionKeyPair = CryptUtil.generateEcdsa384KeyPair();
                final String multiplayerToken = Jwts.builder()
                        .signWith(sessionKeyPair.getPrivate(), Jwts.SIG.ES384)
                        .claim("cpk", Base64.getEncoder().encodeToString(sessionKeyPair.getPublic().getEncoded())) // client public key
                        .claim("xid", String.valueOf(Math.abs(FNV1.fnv1_64(username.getBytes(StandardCharsets.UTF_8))))) // xuid
                        .claim("xname", username) // display name
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(now.plus(4, ChronoUnit.HOURS)))
                        .compact();
                wrapper.user().put(new AuthData(multiplayerToken, sessionKeyPair, UUID.randomUUID()));
            }

            final AuthData authData = wrapper.user().get(AuthData.class);
            final Jwt multiplayerTokenJwt = Jwt.parse(authData.getMultiplayerToken());
            authData.setDisplayName(multiplayerTokenJwt.payload().get("xname").getAsString());
            authData.setXuid(multiplayerTokenJwt.payload().get("xid").getAsString());

            final ProtocolInfo protocolInfo = wrapper.user().getProtocolInfo();
            protocolInfo.setUsername(authData.getDisplayName());
            protocolInfo.setUuid(UUID.nameUUIDFromBytes(("pocket-auth-1-xuid:" + authData.getXuid()).getBytes(StandardCharsets.UTF_8)));

            if (authData.getSkinJwt() == null) {
                final KeyPair sessionKeyPair = authData.getSessionKeyPair();
                authData.setSkinJwt(Jwts.builder()
                        .signWith(sessionKeyPair.getPrivate(), Jwts.SIG.ES384)
                        .header().add("x5u", Base64.getEncoder().encodeToString(sessionKeyPair.getPublic().getEncoded())).and()
                        .claims(Via.getManager().getProviders().get(SkinProvider.class).getClientPlayerSkin(wrapper.user()))
                        .compact());
            }
        });
        protocol.registerServerboundTransition(ServerboundLoginPackets.LOGIN_ACKNOWLEDGED, null, PacketWrapper::cancel);
    }

    private static SecretKey ecdhKeyExchange(final PrivateKey localPrivateKey, final Key remotePublicKey, final byte[] salt) {
        try {
            final KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
            ecdh.init(localPrivateKey);
            ecdh.doPhase(remotePublicKey, true);
            final byte[] sharedSecret = ecdh.generateSecret();

            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(salt);
            sha256.update(sharedSecret);
            return new SecretKeySpec(sha256.digest(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to perform ECDH key exchange", e);
        }
    }

}
