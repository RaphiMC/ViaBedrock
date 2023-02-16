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
package net.raphimc.viabedrockplugin.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.protocol.providers.NettyPipelineProvider;
import net.raphimc.viabedrockplugin.BedrockProxyConnection;
import net.raphimc.viaproxy.proxy.ProxyConnection;

import javax.crypto.SecretKey;

public class ViaProxyNettyPipelineProvider extends NettyPipelineProvider {

    @Override
    public void enableCompression(UserConnection user, int threshold, int algorithm) {
        final BedrockProxyConnection proxyConnection = (BedrockProxyConnection) ProxyConnection.fromUserConnection(user);

        try {
            switch (algorithm) {
                case 0:
                    proxyConnection.enableZLibCompression();
                    break;
                case 1:
                    proxyConnection.enableSnappyCompression();
                    break;
                default:
                    throw new IllegalStateException("Invalid compression algorithm: " + algorithm);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enableEncryption(UserConnection user, SecretKey key) {
        final BedrockProxyConnection proxyConnection = (BedrockProxyConnection) ProxyConnection.fromUserConnection(user);
        try {
            proxyConnection.enableAesGcmEncryption(key);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
