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
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.util.Pair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.gson.io.GsonDeserializer;
import io.netty.util.AsciiString;
import net.raphimc.viabedrock.api.WideSteveSkinProvider;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.providers.NettyPipelineProvider;
import net.raphimc.viabedrock.protocol.storage.AuthChainData;
import net.raphimc.viabedrock.protocol.storage.HandshakeStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

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

    private static final String MOJANG_PUBLIC_KEY_BASE64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";
    private static final ECPublicKey MOJANG_PUBLIC_KEY;
    public static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).disableHtmlEscaping().create();
    public static final GsonDeserializer<Map<String, ?>> GSON_DESERIALIZER = new GsonDeserializer<>(GSON);

    static {
        try {
            MOJANG_PUBLIC_KEY = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(MOJANG_PUBLIC_KEY_BASE64)));
        } catch (Throwable e) {
            throw new RuntimeException("Could not create Mojang public key", e);
        }
    }

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(State.LOGIN, ClientboundBedrockPackets.NETWORK_SETTINGS.getId(), -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                read(BedrockTypes.UNSIGNED_SHORT_LE); // compression threshold
                handler(wrapper -> {
                    wrapper.cancel();
                    final HandshakeStorage handshakeStorage = wrapper.user().get(HandshakeStorage.class);

                    final int algorithm = wrapper.read(BedrockTypes.UNSIGNED_SHORT_LE); // compression algorithm
                    Via.getManager().getProviders().get(NettyPipelineProvider.class).enableCompression(wrapper.user(), algorithm);

                    final Pair<String, String> chainAndSkin = getLoginData(wrapper.user());

                    final PacketWrapper login = PacketWrapper.create(ServerboundBedrockPackets.LOGIN, wrapper.user());
                    login.write(Type.INT, handshakeStorage.getProtocolVersion()); // protocol version
                    login.write(BedrockTypes.UNSIGNED_VAR_INT, chainAndSkin.key().length() + chainAndSkin.value().length() + 8L); // length
                    login.write(BedrockTypes.ASCII_STRING, AsciiString.of(chainAndSkin.key())); // chain data
                    login.write(BedrockTypes.ASCII_STRING, AsciiString.of(chainAndSkin.value())); // skin data
                    login.sendToServer(BedrockProtocol.class);
                });
            }
        });

        protocol.registerServerbound(State.LOGIN, -1, ServerboundLoginPackets.HELLO.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.cancel();

                    final ProtocolInfo protocolInfo = wrapper.user().getProtocolInfo();
                    protocolInfo.setUsername(wrapper.read(Type.STRING));
                    protocolInfo.setUuid(wrapper.read(Type.OPTIONAL_UUID));
                });
            }
        });
    }

    private static Pair<String, String> getLoginData(final UserConnection user) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        if (user.has(AuthChainData.class)) {
            final AuthChainData authChainData = user.get(AuthChainData.class);

            final PublicKey publicKey = authChainData.getPublicKey();
            final PrivateKey privateKey = authChainData.getPrivateKey();
            final String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            final Jws<Claims> mojangJwt = Jwts.parserBuilder().setSigningKey(MOJANG_PUBLIC_KEY).deserializeJsonWith(GSON_DESERIALIZER).build().parseClaimsJws(authChainData.getMojangJwt());
            final ECPublicKey mojangJwtPublicKey = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(mojangJwt.getBody().get("identityPublicKey", String.class))));
            final Jws<Claims> identityJwt = Jwts.parserBuilder().setSigningKey(mojangJwtPublicKey).build().parseClaimsJws(authChainData.getIdentityJwt());

            final String selfSignedJwt = Jwts.builder()
                    .signWith(privateKey, SignatureAlgorithm.ES384)
                    .setHeaderParam("x5u", encodedPublicKey)
                    .claim("certificateAuthority", true)
                    .claim("identityPublicKey", mojangJwt.getHeader().get("x5u"))
                    .setExpiration(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)))
                    .setNotBefore(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                    .compact();

            final JsonObject rootObj = new JsonObject();
            final JsonArray chain = new JsonArray();
            chain.add(new JsonPrimitive(selfSignedJwt));
            chain.add(new JsonPrimitive(authChainData.getMojangJwt()));
            chain.add(new JsonPrimitive(authChainData.getIdentityJwt()));
            rootObj.add("chain", chain);
            final String chainData = rootObj.toString();

            final String skinData = Jwts.builder()
                    .signWith(privateKey, SignatureAlgorithm.ES384)
                    .setHeaderParam("x5u", encodedPublicKey)
                    .addClaims(WideSteveSkinProvider.get(user))
                    .compact();

            final Map<String, Object> extraData = identityJwt.getBody().get("extraData", Map.class);
            authChainData.setXuid((String) extraData.get("XUID"));
            authChainData.setIdentity(UUID.fromString((String) extraData.get("identity")));
            authChainData.setDisplayName((String) extraData.get("displayName"));

            return new Pair<>(chainData, skinData);
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

            final JsonObject rootObj = new JsonObject();
            final JsonArray chain = new JsonArray();
            chain.add(new JsonPrimitive(identityJwt));
            rootObj.add("chain", chain);
            final String chainData = rootObj.toString();

            final String skinData = Jwts.builder()
                    .signWith(privateKey, SignatureAlgorithm.ES384)
                    .setHeaderParam("x5u", encodedPublicKey)
                    .addClaims(WideSteveSkinProvider.get(user))
                    .compact();

            final AuthChainData authChainData = new AuthChainData(user, null, null, publicKey, privateKey);
            authChainData.setXuid((String) extraData.get("XUID"));
            authChainData.setIdentity(UUID.fromString((String) extraData.get("identity")));
            authChainData.setDisplayName((String) extraData.get("displayName"));
            user.put(authChainData);

            return new Pair<>(chainData, skinData);
        }
    }

}
