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

import java.util.HashMap;
import java.util.Map;

public class BlobCacheProvider implements Provider {

    private final Map<Long, byte[]> blobs = new HashMap<>();

    public BlobCacheProvider() {
        this.blobs.put(0L, new byte[0]);
    }

    public byte[] addBlob(final UserConnection user, final long hash, final byte[] compressedBlob) {
        synchronized (this.blobs) {
            return this.blobs.put(hash, compressedBlob);
        }
    }

    public boolean hasBlob(final UserConnection user, final long hash) {
        synchronized (this.blobs) {
            return this.blobs.containsKey(hash);
        }
    }

    public byte[] getBlob(final UserConnection user, final long hash) {
        synchronized (this.blobs) {
            return this.blobs.get(hash);
        }
    }

}
