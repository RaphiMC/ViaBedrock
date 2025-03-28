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
package net.raphimc.viabedrock.protocol.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.modinterface.BedrockSkinUtilityInterface;
import net.raphimc.viabedrock.api.modinterface.ViaBedrockUtilityInterface;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.ProtocolConstants;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.*;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.storage.AuthChainData;
import net.raphimc.viabedrock.protocol.storage.ChannelStorage;
import net.raphimc.viabedrock.protocol.storage.HandshakeStorage;
import net.raphimc.viabedrock.protocol.types.primitive.ImageType;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkinProvider implements Provider {

    public Map<String, Object> getClientPlayerSkin(final UserConnection user) {
        final HandshakeStorage handshakeStorage = user.get(HandshakeStorage.class);
        final AuthChainData authChainData = user.get(AuthChainData.class);

        final ResourcePack.Content skinPackContent = BedrockProtocol.MAPPINGS.getBedrockVanillaResourcePacks().get("vanilla_skin_pack").content();
        final BufferedImage skin = skinPackContent.getImage("steve.png").getImage();
        final JsonObject skinGeometry = JsonUtil.sort(skinPackContent.getJson("geometry.json"), Comparator.naturalOrder());

        final Map<String, Object> claims = new HashMap<>();
        claims.put("PlayFabId", authChainData.getPlayFabId().toLowerCase(Locale.ROOT));
        claims.put("SkinId", "Custom" + authChainData.getDeviceId());
        claims.put("SkinResourcePatch", Base64.getEncoder().encodeToString("{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}".getBytes(StandardCharsets.UTF_8)));
        claims.put("SkinImageWidth", skin.getWidth());
        claims.put("SkinImageHeight", skin.getHeight());
        claims.put("SkinData", Base64.getEncoder().encodeToString(ImageType.getImageData(skin)));
        claims.put("AnimatedImageData", new ArrayList<>());
        claims.put("CapeImageHeight", 0);
        claims.put("CapeImageWidth", 0);
        claims.put("CapeData", "");
        claims.put("CapeId", "");
        claims.put("SkinGeometryData", Base64.getEncoder().encodeToString(skinGeometry.toString().getBytes(StandardCharsets.UTF_8)));
        claims.put("SkinGeometryDataEngineVersion", Base64.getEncoder().encodeToString("0.0.0".getBytes(StandardCharsets.UTF_8)));
        claims.put("SkinAnimationData", "");
        claims.put("ArmSize", "wide");
        claims.put("SkinColor", "#0");
        claims.put("PersonaPieces", new ArrayList<>());
        claims.put("PieceTintColors", new ArrayList<>());
        claims.put("PremiumSkin", false);
        claims.put("PersonaSkin", false);
        claims.put("CapeOnClassicSkin", false);
        claims.put("ClientRandomId", ThreadLocalRandom.current().nextLong()); // ?
        claims.put("SelfSignedId", UUID.randomUUID().toString()); // ?
        claims.put("CurrentInputMode", InputMode.Mouse.getValue());
        claims.put("DefaultInputMode", InputMode.Mouse.getValue());
        claims.put("GuiScale", -1L);
        claims.put("UIProfile", UIProfile.Classic.getValue());
        claims.put("DeviceId", authChainData.getDeviceId().toString());
        claims.put("DeviceModel", "");
        claims.put("DeviceOS", BuildPlatform.Google.getValue());
        claims.put("LanguageCode", "en_US");
        claims.put("MaxViewDistance", 96);
        claims.put("MemoryTier", MemoryTier.SuperHigh.ordinal());
        claims.put("GraphicsMode", GraphicsMode.Fancy.getValue());
        claims.put("PlatformType", 0);
        claims.put("PlatformOfflineId", "");
        claims.put("PlatformOnlineId", "");
        claims.put("GameVersion", ProtocolConstants.BEDROCK_VERSION_NAME);
        claims.put("ServerAddress", handshakeStorage.hostname() + ":" + handshakeStorage.port());
        claims.put("ThirdPartyName", user.getProtocolInfo().getUsername());
        claims.put("ThirdPartyNameOnly", false);
        claims.put("IsEditorMode", false);
        claims.put("TrustedSkin", false);
        claims.put("OverrideSkin", false);
        claims.put("CompatibleWithClientSideChunkGen", false);
        return claims;
    }

    public void setSkin(final UserConnection user, final UUID playerUuid, final SkinData skin) {
        final ChannelStorage channelStorage = user.get(ChannelStorage.class);
        if (channelStorage.hasChannel(ViaBedrockUtilityInterface.CHANNEL)) {
            ViaBedrockUtilityInterface.sendSkin(user, playerUuid, skin);
        } else if (channelStorage.hasChannel(BedrockSkinUtilityInterface.CHANNEL)) {
            BedrockSkinUtilityInterface.sendSkin(user, playerUuid, skin);
        }
    }

}
