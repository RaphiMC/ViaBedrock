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

import com.google.gson.*;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundHandshakePackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import io.jsonwebtoken.*;
import io.jsonwebtoken.gson.io.GsonDeserializer;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.io.compression.ProtocolCompression;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.ServerBlacklist;
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
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LoginPackets {

    private static final KeyFactory EC_KEYFACTORY;
    private static final String MOJANG_PUBLIC_KEY_BASE64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAECRXueJeTDqNRRgJi/vlRufByu/2G0i2Ebt6YMar5QX/R0DIIyrJMcUpruK4QveTfJSTp3Shlq4Gk34cD/4GUWwkv0DVuzeuB+tXija7HBxii03NHDbPAD0AKnLr2wdAp";
    private static final ECPublicKey MOJANG_PUBLIC_KEY;
    private static final int CLOCK_SKEW = 60;

    private static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).disableHtmlEscaping().create();
    private static final GsonDeserializer<Map<String, ?>> GSON_DESERIALIZER = new GsonDeserializer<>(GSON);

    static {
        try {
            EC_KEYFACTORY = KeyFactory.getInstance("EC");
            MOJANG_PUBLIC_KEY = publicKeyFromBase64(MOJANG_PUBLIC_KEY_BASE64);
        } catch (Throwable e) {
            throw new RuntimeException("Could not initialize the required cryptography", e);
        }
    }

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

            final JsonObject certificateChainObj = new JsonObject();
            final JsonArray chain = new JsonArray();
            if (authData.getSelfSignedJwt() != null) {
                chain.add(new JsonPrimitive(authData.getSelfSignedJwt()));
            }
            if (authData.getMojangJwt() != null) {
                chain.add(new JsonPrimitive(authData.getMojangJwt()));
            }
            if (authData.getIdentityJwt() != null) {
                chain.add(new JsonPrimitive(authData.getIdentityJwt()));
            }
            certificateChainObj.add("chain", chain);

            final JsonObject authInfoObj = new JsonObject();
            authInfoObj.addProperty("AuthenticationType", authData.getMojangJwt() != null ? AuthenticationType.Full.ordinal() : AuthenticationType.SelfSigned.ordinal());
            authInfoObj.addProperty("Certificate", certificateChainObj.toString());
            authInfoObj.addProperty("Token", "");
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

            final Jws<Claims> jwt = Jwts.parser()
                    .clockSkewSeconds(CLOCK_SKEW)
                    .keyLocator(new LocatorAdapter<>() {
                        @Override
                        protected Key locate(ProtectedHeader header) {
                            return publicKeyFromBase64((String) header.get("x5u"));
                        }
                    })
                    .build()
                    .parseSignedClaims(wrapper.read(BedrockTypes.STRING)); // jwt

            try {
                final byte[] salt = Base64.getDecoder().decode(jwt.getPayload().get("salt", String.class));
                final SecretKey secretKey = ecdhKeyExchange((ECPrivateKey) authData.getSessionKeyPair().getPrivate(), publicKeyFromBase64((String) jwt.getHeader().get("x5u")), salt);
                Via.getManager().getProviders().get(NettyPipelineProvider.class).enableEncryption(wrapper.user(), secretKey);
            } catch (Throwable e) {
                throw new RuntimeException("Could not enable encryption", e);
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

    private static ECPublicKey publicKeyFromBase64(final String base64) {
        try {
            return (ECPublicKey) EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Could not decode base64 public key", e);
        }
    }

    private static void validateAndFillAuthData(final UserConnection user) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        if (user.has(AuthData.class)) { // Externally supplied auth data
            final AuthData authData = user.get(AuthData.class);

            final KeyPair sessionKeyPair = authData.getSessionKeyPair();
            final String encodedPublicKey = Base64.getEncoder().encodeToString(sessionKeyPair.getPublic().getEncoded());

            final Jws<Claims> mojangJwt = Jwts.parser().clockSkewSeconds(CLOCK_SKEW).verifyWith(MOJANG_PUBLIC_KEY).json(GSON_DESERIALIZER).build().parseSignedClaims(authData.getMojangJwt());
            final ECPublicKey mojangJwtPublicKey = publicKeyFromBase64(mojangJwt.getPayload().get("identityPublicKey", String.class));
            final Jws<Claims> identityJwt = Jwts.parser().clockSkewSeconds(CLOCK_SKEW).verifyWith(mojangJwtPublicKey).build().parseSignedClaims(authData.getIdentityJwt());

            if (authData.getSelfSignedJwt() == null) {
                final String selfSignedJwt = Jwts.builder()
                        .signWith(sessionKeyPair.getPrivate(), Jwts.SIG.ES384)
                        .header().add("x5u", encodedPublicKey).and()
                        .claim("certificateAuthority", true)
                        .claim("identityPublicKey", mojangJwt.getHeader().get("x5u"))
                        .expiration(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)))
                        .notBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                        .compact();

                authData.setSelfSignedJwt(selfSignedJwt);
            }
            if (authData.getSkinJwt() == null) {
                final String skinData = Jwts.builder()
                        .signWith(sessionKeyPair.getPrivate(), Jwts.SIG.ES384)
                        .header().add("x5u", encodedPublicKey).and()
                        .claims(Via.getManager().getProviders().get(SkinProvider.class).getClientPlayerSkin(user))
                        .compact();

                authData.setSkinJwt(skinData);
            }

            final Map<String, Object> extraData = identityJwt.getPayload().get("extraData", Map.class);
            authData.setDisplayName((String) extraData.get("displayName"));
            authData.setXuid((String) extraData.get("XUID"));
        } else {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(new ECGenParameterSpec("secp384r1"));
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            final String encodedPublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            final String displayName = user.getProtocolInfo().getUsername();
            final UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + displayName).getBytes(StandardCharsets.UTF_8));

            final Map<String, Object> extraData = new HashMap<>();
            extraData.put("XUID", Long.toString(offlineUUID.getLeastSignificantBits()));
            extraData.put("identity", offlineUUID.toString());
            extraData.put("displayName", displayName);
            extraData.put("titleId", "1739947436"); // Android title id
            extraData.put("sandboxId", "RETAIL");

            final String identityJwt = Jwts.builder()
                    .signWith(keyPair.getPrivate(), Jwts.SIG.ES384)
                    .header().add("x5u", encodedPublicKey).and()
                    .claim("identityPublicKey", encodedPublicKey)
                    .claim("randomNonce", ThreadLocalRandom.current().nextLong())
                    .claim("extraData", extraData)
                    .issuer("Mojang")
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                    .notBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                    .compact();

            final AuthData authData = new AuthData(null, identityJwt, null, keyPair, UUID.randomUUID());
            authData.setDisplayName((String) extraData.get("displayName"));
            authData.setXuid((String) extraData.get("XUID"));
            user.put(authData);

            final String skinData = Jwts.builder()
                    .signWith(keyPair.getPrivate(), Jwts.SIG.ES384)
                    .header().add("x5u", encodedPublicKey).and()
                    .claims(Via.getManager().getProviders().get(SkinProvider.class).getClientPlayerSkin(user))
                    .compact();

            authData.setSkinJwt(skinData);
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
