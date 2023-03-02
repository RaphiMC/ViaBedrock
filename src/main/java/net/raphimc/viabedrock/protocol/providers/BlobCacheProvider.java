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
import net.raphimc.viabedrock.protocol.storage.BlobCache;

import java.util.concurrent.CompletableFuture;

public class BlobCacheProvider implements Provider {

    public void addBlob(final UserConnection user, final long hash, final byte[] blob) {
        user.get(BlobCache.class).addBlob(hash, blob);
    }

    public boolean hasBlob(final UserConnection user, final long... hashes) {
        return user.get(BlobCache.class).hasBlob(hashes);
    }

    public CompletableFuture<byte[]> getBlob(final UserConnection user, final long... hashes) {
        return user.get(BlobCache.class).getBlob(hashes);
    }

    public CompletableFuture<byte[]> getBlob(final UserConnection user, final Long[] hashes) {
        final long[] longs = new long[hashes.length];
        for (int i = 0; i < hashes.length; i++) {
            longs[i] = hashes[i];
        }
        return this.getBlob(user, longs);
    }

}
