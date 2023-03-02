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

import com.google.gson.*;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import io.jsonwebtoken.*;
import io.jsonwebtoken.gson.io.GsonDeserializer;
import io.jsonwebtoken.io.Decoders;
import io.netty.util.AsciiString;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.WideSteveSkinProvider;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PlayStatus;
import net.raphimc.viabedrock.protocol.providers.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.storage.AuthChainData;
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
import java.util.logging.Level;

public class LoginPackets {

    private static final KeyFactory EC_KEYFACTORY;
    private static final ECPublicKey MOJANG_PUBLIC_KEY;

    private static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).disableHtmlEscaping().create();
    private static final GsonDeserializer<Map<String, ?>> GSON_DESERIALIZER = new GsonDeserializer<>(GSON);

    static {
        try {
            EC_KEYFACTORY = KeyFactory.getInstance("EC");
            MOJANG_PUBLIC_KEY = publicKeyFromBase64("MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V");
        } catch (Throwable e) {
            throw new RuntimeException("Could not initialize the required cryptography", e);
        }
    }

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(State.LOGIN, ClientboundBedrockPackets.DISCONNECT.getId(), ClientboundLoginPackets.LOGIN_DISCONNECT.getId(), new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final boolean hasMessage = !wrapper.read(Type.BOOLEAN); // skip message
                    if (hasMessage) {
                        final String rawMessage = wrapper.read(BedrockTypes.STRING);
                        final String translatedMessage = protocol.getMappingData().getTranslations().getOrDefault(rawMessage, rawMessage);
                        wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(translatedMessage)); // reason
                    } else {
                        wrapper.write(Type.COMPONENT, com.viaversion.viaversion.libs.gson.JsonNull.INSTANCE); // reason
                    }
                });
            }
        });
        protocol.registerClientbound(State.LOGIN, ClientboundBedrockPackets.NETWORK_SETTINGS.getId(), -1, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final HandshakeStorage handshakeStorage = wrapper.user().get(HandshakeStorage.class);
                    final AuthChainData authChainData = wrapper.user().get(AuthChainData.class);

                    final int threshold = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // compression threshold
                    final int algorithm = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // compression algorithm
                    Via.getManager().getProviders().get(NettyPipelineProvider.class).enableCompression(wrapper.user(), threshold, algorithm);

                    final JsonObject rootObj = new JsonObject();
                    final JsonArray chain = new JsonArray();
                    if (authChainData.getSelfSignedJwt() != null) {
                        chain.add(new JsonPrimitive(authChainData.getSelfSignedJwt()));
                    }
                    if (authChainData.getMojangJwt() != null) {
                        chain.add(new JsonPrimitive(authChainData.getMojangJwt()));
                    }
                    if (authChainData.getIdentityJwt() != null) {
                        chain.add(new JsonPrimitive(authChainData.getIdentityJwt()));
                    }
                    rootObj.add("chain", chain);
                    final String chainData = rootObj.toString();

                    final PacketWrapper login = PacketWrapper.create(ServerboundBedrockPackets.LOGIN, wrapper.user());
                    login.write(Type.INT, handshakeStorage.getProtocolVersion()); // protocol version
                    login.write(BedrockTypes.UNSIGNED_VAR_INT, chainData.length() + authChainData.getSkinJwt().length() + 8); // length
                    login.write(BedrockTypes.ASCII_STRING, AsciiString.of(chainData)); // chain data
                    login.write(BedrockTypes.ASCII_STRING, AsciiString.of(authChainData.getSkinJwt())); // skin data
                    login.sendToServer(BedrockProtocol.class);
                });
            }
        });
        protocol.registerClientbound(State.LOGIN, ClientboundBedrockPackets.SERVER_TO_CLIENT_HANDSHAKE.getId(), -1, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.cancel();
                    final AuthChainData authChainData = wrapper.user().get(AuthChainData.class);

                    // So basically jjwt is shit and can't trim the header/body payload.
                    // This causes it to think the payload isn't json and throw errors. To fix that we need to trim only the payload.
                    // https://github.com/jwtk/jjwt/issues/687
                    final boolean[] trim = {true};
                    final Jws<Claims> jwt = Jwts.parserBuilder()
                            .setSigningKeyResolver(new SigningKeyResolverAdapter() {
                                @Override
                                public Key resolveSigningKey(JwsHeader header, Claims claims) {
                                    trim[0] = false;
                                    return publicKeyFromBase64((String) header.get("x5u"));
                                }
                            })
                            .base64UrlDecodeWith(s -> {
                                if (trim[0]) {
                                    return new String(Decoders.BASE64URL.decode(s), StandardCharsets.UTF_8).trim().getBytes(StandardCharsets.UTF_8);
                                } else {
                                    return Decoders.BASE64URL.decode(s);
                                }
                            })
                            .build()
                            .parseClaimsJws(wrapper.read(BedrockTypes.STRING)); // jwt

                    final byte[] salt = Base64.getDecoder().decode(jwt.getBody().get("salt", String.class));
                    final SecretKey secretKey = ecdhKeyExchange(authChainData.getPrivateKey(), publicKeyFromBase64((String) jwt.getHeader().get("x5u")), salt);
                    Via.getManager().getProviders().get(NettyPipelineProvider.class).enableEncryption(wrapper.user(), secretKey);

                    final PacketWrapper clientToServerHandshake = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_TO_SERVER_HANDSHAKE, wrapper.user());
                    clientToServerHandshake.sendToServer(BedrockProtocol.class);
                });
            }
        });
        protocol.registerClientbound(State.LOGIN, ClientboundBedrockPackets.PLAY_STATUS.getId(), ClientboundLoginPackets.GAME_PROFILE.getId(), new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final int status = wrapper.read(Type.INT); // status

                    if (status == PlayStatus.LOGIN_SUCCESS) {
                        final AuthChainData authChainData = wrapper.user().get(AuthChainData.class);
                        wrapper.write(Type.UUID, authChainData.getIdentity()); // uuid
                        wrapper.write(Type.STRING, authChainData.getDisplayName()); // username
                        wrapper.write(Type.VAR_INT, 0); // properties length

                        final ProtocolInfo protocolInfo = wrapper.user().getProtocolInfo();
                        protocolInfo.setUsername(authChainData.getDisplayName());
                        protocolInfo.setUuid(authChainData.getIdentity());

                        protocolInfo.setState(State.PLAY);
                        Via.getManager().getConnectionManager().onLoginSuccess(wrapper.user());
                        if (!protocolInfo.getPipeline().hasNonBaseProtocols()) {
                            wrapper.user().setActive(false);
                        }

                        final PacketWrapper clientCacheStatus = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_CACHE_STATUS, wrapper.user());
                        clientCacheStatus.write(Type.BOOLEAN, ViaBedrock.getConfig().isBlobCacheEnabled()); // is supported
                        clientCacheStatus.sendToServer(BedrockProtocol.class);
                    } else {
                        wrapper.setPacketType(ClientboundLoginPackets.LOGIN_DISCONNECT);
                        writePlayStatusKickMessage(wrapper, status);
                    }
                });
            }
        });

        protocol.registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundBedrockPackets.REQUEST_NETWORK_SETTINGS.getId(), new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    final HandshakeStorage handshakeStorage = wrapper.user().get(HandshakeStorage.class);

                    final ProtocolInfo protocolInfo = wrapper.user().getProtocolInfo();
                    protocolInfo.setUsername(wrapper.read(Type.STRING));
                    protocolInfo.setUuid(wrapper.read(Type.OPTIONAL_UUID));

                    wrapper.write(Type.INT, handshakeStorage.getProtocolVersion()); // protocol version

                    validateAndFullAuthChainData(wrapper.user());
                });
            }
        });
    }

    public static void writePlayStatusKickMessage(final PacketWrapper wrapper, final int status) {
        switch (status) {
            case PlayStatus.LOGIN_FAILED_CLIENT_OLD:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.outdatedClient")));
                break;
            case PlayStatus.LOGIN_FAILED_SERVER_OLD:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.outdatedServer")));
                break;
            case PlayStatus.LOGIN_FAILED_INVALID_TENANT:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.invalidTenant")));
                break;
            case PlayStatus.LOGIN_FAILED_EDITION_MISMATCH_EDU_TO_VANILLA:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.editionMismatchEduToVanilla")));
                break;
            case PlayStatus.LOGIN_FAILED_EDITION_MISMATCH_VANILLA_TO_EDU:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.editionMismatchVanillaToEdu")));
                break;
            case PlayStatus.FAILED_SERVER_FULL_SUB_CLIENT:
            case PlayStatus.VANILLA_TO_EDITOR_MISMATCH:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.serverFull") + "\n\n\n\n" + BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.serverFull.title")));
                break;
            case PlayStatus.EDITOR_TO_VANILLA_MISMATCH:
                wrapper.write(Type.COMPONENT, JsonUtil.textToComponent(BedrockProtocol.MAPPINGS.getTranslations().get("disconnectionScreen.editor.mismatchEditorToVanilla")));
                break;
            default: // Mojang client silently ignores invalid values
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received invalid login status: " + status);
            case PlayStatus.PLAYER_SPAWN:
            case PlayStatus.LOGIN_SUCCESS:
                wrapper.cancel();
                break;
        }
    }

    private static ECPublicKey publicKeyFromBase64(final String base64) {
        try {
            return (ECPublicKey) EC_KEYFACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Could not decode base64 public key", e);
        }
    }

    private static void validateAndFullAuthChainData(final UserConnection user) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        if (user.has(AuthChainData.class)) { // Externally supplied chain data
            final AuthChainData authChainData = user.get(AuthChainData.class);

            final PublicKey publicKey = authChainData.getPublicKey();
            final PrivateKey privateKey = authChainData.getPrivateKey();
            final String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            final Jws<Claims> mojangJwt = Jwts.parserBuilder().setSigningKey(MOJANG_PUBLIC_KEY).deserializeJsonWith(GSON_DESERIALIZER).build().parseClaimsJws(authChainData.getMojangJwt());
            final ECPublicKey mojangJwtPublicKey = publicKeyFromBase64(mojangJwt.getBody().get("identityPublicKey", String.class));
            final Jws<Claims> identityJwt = Jwts.parserBuilder().setSigningKey(mojangJwtPublicKey).build().parseClaimsJws(authChainData.getIdentityJwt());

            if (authChainData.getSelfSignedJwt() == null) {
                final String selfSignedJwt = Jwts.builder()
                        .signWith(privateKey, SignatureAlgorithm.ES384)
                        .setHeaderParam("x5u", encodedPublicKey)
                        .claim("certificateAuthority", true)
                        .claim("identityPublicKey", mojangJwt.getHeader().get("x5u"))
                        .setExpiration(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)))
                        .setNotBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                        .compact();

                authChainData.setSelfSignedJwt(selfSignedJwt);
            }
            if (authChainData.getSkinJwt() == null) {
                final String skinData = Jwts.builder()
                        .signWith(privateKey, SignatureAlgorithm.ES384)
                        .setHeaderParam("x5u", encodedPublicKey)
                        .addClaims(WideSteveSkinProvider.get(user))
                        .compact();

                authChainData.setSkinJwt(skinData);
            }

            final Map<String, Object> extraData = identityJwt.getBody().get("extraData", Map.class);
            authChainData.setXuid((String) extraData.get("XUID"));
            authChainData.setIdentity(UUID.fromString((String) extraData.get("identity")));
            authChainData.setDisplayName((String) extraData.get("displayName"));
        } else {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(new ECGenParameterSpec("secp384r1"));
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            final ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            final ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
            final String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            final String displayName = user.getProtocolInfo().getUsername();
            final UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + displayName).getBytes(StandardCharsets.UTF_8));

            final Map<String, Object> extraData = new HashMap<>();
            extraData.put("XUID", Long.toString(offlineUUID.getLeastSignificantBits()));
            extraData.put("identity", offlineUUID.toString());
            extraData.put("displayName", displayName);
            extraData.put("titleId", "896928775");

            final String identityJwt = Jwts.builder()
                    .signWith(privateKey, SignatureAlgorithm.ES384)
                    .setHeaderParam("x5u", encodedPublicKey)
                    .claim("identityPublicKey", encodedPublicKey)
                    .claim("iss", "Mojang")
                    .claim("randomNonce", ThreadLocalRandom.current().nextLong())
                    .claim("extraData", extraData)
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                    .setNotBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                    .compact();

            final String skinData = Jwts.builder()
                    .signWith(privateKey, SignatureAlgorithm.ES384)
                    .setHeaderParam("x5u", encodedPublicKey)
                    .addClaims(WideSteveSkinProvider.get(user))
                    .compact();

            final AuthChainData authChainData = new AuthChainData(user, null, identityJwt, publicKey, privateKey);
            authChainData.setSkinJwt(skinData);
            authChainData.setXuid((String) extraData.get("XUID"));
            authChainData.setIdentity(UUID.fromString((String) extraData.get("identity")));
            authChainData.setDisplayName((String) extraData.get("displayName"));
            user.put(authChainData);
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
