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
package net.raphimc.viabedrock.protocol.providers.impl;

import com.google.common.primitives.Longs;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.LZ4;
import net.raphimc.viabedrock.protocol.providers.BlobCacheProvider;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DiskBlobCacheProvider extends BlobCacheProvider {

    private static final byte[] BLOB_KEY_PREFIX = "blob_".getBytes(StandardCharsets.US_ASCII);

    @Override
    public byte[] addBlob(final long hash, final byte[] blob) {
        final byte[] oldBlob = this.getBlob(hash);
        if (Arrays.equals(blob, oldBlob)) {
            return oldBlob;
        }

        ViaBedrock.getBlobCache().put(this.createBlobKey(hash), LZ4.compress(blob));
        return oldBlob;
    }

    @Override
    public boolean hasBlob(final long hash) {
        return ViaBedrock.getBlobCache().get(this.createBlobKey(hash)) != null;
    }

    @Override
    public byte[] getBlob(final long hash) {
        return LZ4.decompress(ViaBedrock.getBlobCache().get(this.createBlobKey(hash)));
    }

    private byte[] createBlobKey(final long hash) {
        final byte[] hashKey = Longs.toByteArray(Long.reverseBytes(hash));
        final byte[] blobKey = new byte[BLOB_KEY_PREFIX.length + hashKey.length];
        System.arraycopy(BLOB_KEY_PREFIX, 0, blobKey, 0, BLOB_KEY_PREFIX.length);
        System.arraycopy(hashKey, 0, blobKey, BLOB_KEY_PREFIX.length, hashKey.length);
        return blobKey;
    }

}
