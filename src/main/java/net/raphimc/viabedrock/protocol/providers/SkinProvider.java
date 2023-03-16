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
package net.raphimc.viabedrock.protocol.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.api.modinterface.BedrockSkinUtilityInterface;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.DeviceOS;
import net.raphimc.viabedrock.protocol.model.SkinData;
import net.raphimc.viabedrock.protocol.storage.ChannelStorage;
import net.raphimc.viabedrock.protocol.storage.HandshakeStorage;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SkinProvider implements Provider {

    public Map<String, Object> getClientPlayerSkin(final UserConnection user) {
        final HandshakeStorage handshakeStorage = user.get(HandshakeStorage.class);
        final String username = user.getProtocolInfo().getUsername();

        final BufferedImage skin = BedrockProtocol.MAPPINGS.getSteveSkin();

        final Map<String, Object> claims = new HashMap<>();

        claims.put("PlayFabId", "");
        claims.put("SkinId", UUID.randomUUID() + ".Custom" + UUID.randomUUID());
        claims.put("SkinResourcePatch", Base64.getEncoder().encodeToString("{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}".getBytes(StandardCharsets.UTF_8)));
        claims.put("SkinImageWidth", skin.getWidth());
        claims.put("SkinImageHeight", skin.getHeight());
        claims.put("SkinData", Base64.getEncoder().encodeToString(this.encodeImage(skin)));
        claims.put("AnimatedImageData", new ArrayList<>());
        claims.put("CapeImageHeight", 0);
        claims.put("CapeImageWidth", 0);
        claims.put("CapeData", "");
        claims.put("CapeId", "");
        claims.put("SkinGeometryData", Base64.getEncoder().encodeToString(BedrockProtocol.MAPPINGS.getSkinGeometry().toString().getBytes(StandardCharsets.UTF_8)));
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
        claims.put("SelfSignedId", UUID.randomUUID().toString());
        claims.put("CurrentInputMode", 1);
        claims.put("DefaultInputMode", 1);
        claims.put("GuiScale", -1L);
        claims.put("UIProfile", 0);
        claims.put("DeviceId", UUID.randomUUID().toString());
        claims.put("DeviceModel", "");
        claims.put("DeviceOS", DeviceOS.UWP.ordinal());
        claims.put("LanguageCode", "en_US");
        claims.put("PlatformOfflineId", "");
        claims.put("PlatformOnlineId", "");
        claims.put("GameVersion", BedrockProtocolVersion.LATEST_BEDROCK_VERSION);
        claims.put("ServerAddress", handshakeStorage.getHostname() + ":" + handshakeStorage.getPort());
        claims.put("ThirdPartyName", username);
        claims.put("ThirdPartyNameOnly", false);
        claims.put("IsEditorMode", false);
        claims.put("TrustedSkin", false);

        return claims;
    }

    public void setSkin(final UserConnection user, final UUID playerUuid, final SkinData skin) throws Exception {
        final ChannelStorage channelStorage = user.get(ChannelStorage.class);
        if (channelStorage.hasChannel(BedrockSkinUtilityInterface.CHANNEL)) {
            BedrockSkinUtilityInterface.sendSkin(user, playerUuid, skin);
        }
    }

    protected byte[] encodeImage(final BufferedImage image) {
        final byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int pixel = image.getRGB(x, y);
                final int index = (y * image.getWidth() + x) * 4;
                data[index] = (byte) ((pixel >> 16) & 0xFF);
                data[index + 1] = (byte) ((pixel >> 8) & 0xFF);
                data[index + 2] = (byte) (pixel & 0xFF);
                data[index + 3] = (byte) ((pixel >> 24) & 0xFF);
            }
        }
        return data;
    }

}
