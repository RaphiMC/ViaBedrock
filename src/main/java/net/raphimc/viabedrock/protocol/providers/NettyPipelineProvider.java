/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
import net.raphimc.viabedrock.api.io.compression.ProtocolCompression;

import javax.crypto.SecretKey;

public abstract class NettyPipelineProvider implements Provider {

    /**
     * Enables compression/decompression for the given user
     *
     * @param user The user
     * @param protocolCompression The protocol compression
     */
    public abstract void enableCompression(final UserConnection user, final ProtocolCompression protocolCompression);

    /**
     * Enables encryption/decryption for the given user
     *
     * @param user The user
     * @param key  The encryption key
     */
    public abstract void enableEncryption(final UserConnection user, final SecretKey key);

}
