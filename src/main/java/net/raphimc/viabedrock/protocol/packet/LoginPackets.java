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
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundHandshakePackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.io.compression.ProtocolCompression;
import net.raphimc.viabedrock.api.util.*;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AuthenticationType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PacketCompressionAlgorithm;
import net.raphimc.viabedrock.protocol.provider.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.provider.SkinProvider;
import net.raphimc.viabedrock.protocol.storage.AuthData;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import net.raphimc.viabedrock.protocol.storage.HandshakeStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LoginPackets {

    private static final int CLOCK_SKEW = 60;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.NETWORK_SETTINGS, null, wrapper -> {
            wrapper.cancel();
            final GameSessionStorage gameSession = wrapper.user().get(GameSessionStorage.class);
            final HandshakeStorage handshakeStorage = wrapper.user().get(HandshakeStorage.class);
            final AuthData authData = wrapper.user().get(AuthData.class);

            final int threshold = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // compression threshold
            final PacketCompressionAlgorithm algorithm = PacketCompressionAlgorithm.getByValue(wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE), PacketCompressionAlgorithm.None); // compression algorithm
            final ProtocolCompression protocolCompression = new ProtocolCompression(algorithm, threshold);
            if (gameSession.getProtocolCompression() == null) {
                Via.getManager().getProviders().get(NettyPipelineProvider.class).enableCompression(wrapper.user(), protocolCompression);
            } else {
                gameSession.getProtocolCompression().end();
            }
            gameSession.setProtocolCompression(protocolCompression);

            final JsonObject authInfoObj = new JsonObject();
            final List<String> certificateChain = authData.getCertificateChain();
            final boolean fullAuth = authData.getMultiplayerToken() != null || certificateChain.size() == 3;
            authInfoObj.addProperty("AuthenticationType", (fullAuth ? AuthenticationType.Full : AuthenticationType.SelfSigned).ordinal());
            if (!certificateChain.isEmpty()) {
                final JsonObject certificateChainObj = new JsonObject();
                certificateChainObj.add("chain", certificateChain.stream().collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
                authInfoObj.addProperty("Certificate", certificateChainObj.toString());
            } else {
                authInfoObj.addProperty("Certificate", "");
            }
            authInfoObj.addProperty("Token", authData.getMultiplayerToken() != null ? authData.getMultiplayerToken() : "");
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
            final AuthData authData = wrapper.user().get(AuthData.class);
            final Jws<Claims> jwt = Jwts.parser().clockSkewSeconds(CLOCK_SKEW).keyLocator(CryptUtil.X5U_KEY_LOCATOR).build().parseSignedClaims(wrapper.read(BedrockTypes.STRING)); // jwt

            try {
                final byte[] salt = Base64.getDecoder().decode(jwt.getPayload().get("salt", String.class));
                final SecretKey secretKey = ecdhKeyExchange((ECPrivateKey) authData.getSessionKeyPair().getPrivate(), CryptUtil.ecPublicKeyFromBase64((String) jwt.getHeader().get("x5u")), salt);
                Via.getManager().getProviders().get(NettyPipelineProvider.class).enableEncryption(wrapper.user(), secretKey);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to enable encryption", e);
            }

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

            final ProtocolInfo protocolInfo = wrapper.user().getProtocolInfo();
            protocolInfo.setUsername(wrapper.read(Types.STRING));
            protocolInfo.setUuid(wrapper.read(Types.UUID));

            wrapper.write(Types.INT, handshakeStorage.protocolVersion()); // protocol version

            try {
                validateAndFillAuthData(wrapper.user());
            } catch (Throwable e) {
                throw new RuntimeException("Could not validate and fill auth data", e);
            }
        });
        protocol.registerServerboundTransition(ServerboundLoginPackets.LOGIN_ACKNOWLEDGED, null, PacketWrapper::cancel);
    }

    private static void validateAndFillAuthData(final UserConnection user) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        if (user.has(AuthData.class)) { // Externally supplied auth data
            final AuthData authData = user.get(AuthData.class);
            if (authData.getMojangJwt() != null && authData.getSelfSignedJwt() == null) {
                final KeyPair sessionKeyPair = authData.getSessionKeyPair();
                final Jwt mojangJwt = Jwt.parse(authData.getMojangJwt());
                authData.setSelfSignedJwt(Jwts.builder()
                        .signWith(sessionKeyPair.getPrivate(), Jwts.SIG.ES384)
                        .header().add("x5u", Base64.getEncoder().encodeToString(sessionKeyPair.getPublic().getEncoded())).and()
                        .claim("certificateAuthority", true)
                        .claim("identityPublicKey", mojangJwt.header().get("x5u").getAsString())
                        .expiration(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)))
                        .notBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                        .compact());
            }
        } else {
            final KeyPair sessionKeyPair = CryptUtil.generateEcdsa384KeyPair();
            final String encodedPublicKey = Base64.getEncoder().encodeToString(sessionKeyPair.getPublic().getEncoded());
            final long xuid = Math.abs(FNV1.fnv1_64(user.getProtocolInfo().getUsername().getBytes(StandardCharsets.UTF_8)));

            final Map<String, Object> extraData = new HashMap<>();
            extraData.put("displayName", user.getProtocolInfo().getUsername());
            extraData.put("XUID", String.valueOf(xuid));
            extraData.put("identity", UUID.nameUUIDFromBytes(("pocket-auth-1-xuid:" + xuid).getBytes(StandardCharsets.UTF_8)));

            final String identityJwt = Jwts.builder()
                    .signWith(sessionKeyPair.getPrivate(), Jwts.SIG.ES384)
                    .header().add("x5u", encodedPublicKey).and()
                    .claim("identityPublicKey", encodedPublicKey)
                    .claim("extraData", extraData)
                    .expiration(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)))
                    .notBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                    .compact();

            user.put(new AuthData(null, identityJwt, null, sessionKeyPair, UUID.randomUUID()));
        }

        final AuthData authData = user.get(AuthData.class);
        if (authData.getSkinJwt() == null) {
            authData.setSkinJwt(Jwts.builder()
                    .signWith(authData.getSessionKeyPair().getPrivate(), Jwts.SIG.ES384)
                    .header().add("x5u", Base64.getEncoder().encodeToString(authData.getSessionKeyPair().getPublic().getEncoded())).and()
                    .claims(Via.getManager().getProviders().get(SkinProvider.class).getClientPlayerSkin(user))
                    .compact());
        }
        if (authData.getMultiplayerToken() != null) {
            final Jwt multiplayerTokenJwt = Jwt.parse(authData.getMultiplayerToken());
            authData.setDisplayName(multiplayerTokenJwt.payload().get("xname").getAsString());
            authData.setXuid(multiplayerTokenJwt.payload().get("xid").getAsString());
        } else if (authData.getIdentityJwt() != null) {
            final Jwt identityJwt = Jwt.parse(authData.getIdentityJwt());
            final JsonObject extraData = identityJwt.payload().getAsJsonObject("extraData");
            authData.setDisplayName(extraData.get("displayName").getAsString());
            authData.setXuid(extraData.get("XUID").getAsString());
        } else {
            throw new IllegalStateException("No multiplayer token or identity token present to extract display name and XUID from");
        }
    }

    private static SecretKey ecdhKeyExchange(final ECPrivateKey localPrivateKey, final ECPublicKey remotePublicKey, final byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException {
        final KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
        ecdh.init(localPrivateKey);
        ecdh.doPhase(remotePublicKey, true);
        final byte[] sharedSecret = ecdh.generateSecret();

        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(salt);
        sha256.update(sharedSecret);
        return new SecretKeySpec(sha256.digest(), "AES");
    }

}
