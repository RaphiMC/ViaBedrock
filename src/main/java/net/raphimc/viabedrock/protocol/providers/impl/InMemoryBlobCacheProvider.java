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

import net.raphimc.viabedrock.protocol.providers.BlobCacheProvider;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class InMemoryBlobCacheProvider extends BlobCacheProvider {

    private final Map<Long, byte[]> blobs = new ConcurrentHashMap<>();

    private final Deflater deflater;
    private final Inflater inflater;
    private final byte[] compressionBuffer;

    public InMemoryBlobCacheProvider() {
        this(true);
    }

    public InMemoryBlobCacheProvider(final boolean compress) {
        if (compress) {
            this.deflater = new Deflater(Deflater.BEST_SPEED);
            this.inflater = new Inflater();
            this.compressionBuffer = new byte[8192];
        } else {
            this.deflater = null;
            this.inflater = null;
            this.compressionBuffer = null;
        }
    }

    @Override
    public byte[] addBlob(final long hash, final byte[] blob) {
        return this.decompress(this.blobs.put(hash, this.compress(blob)));
    }

    @Override
    public boolean hasBlob(final long hash) {
        return this.blobs.containsKey(hash);
    }

    @Override
    public byte[] getBlob(final long hash) {
        return this.decompress(this.blobs.get(hash));
    }

    private byte[] compress(final byte[] data) {
        if (data == null) return null;
        if (this.deflater == null || data.length == 0) return data;

        this.deflater.setInput(data);
        this.deflater.finish();

        final ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        while (!this.deflater.finished()) {
            final int size = this.deflater.deflate(this.compressionBuffer);
            compressed.write(this.compressionBuffer, 0, size);
        }
        this.deflater.reset();
        return compressed.toByteArray();
    }

    private byte[] decompress(final byte[] compressed) {
        if (compressed == null) return null;
        if (this.inflater == null || compressed.length == 0) return compressed;

        this.inflater.setInput(compressed);
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            while (!this.inflater.finished()) {
                final int size = this.inflater.inflate(this.compressionBuffer);
                data.write(this.compressionBuffer, 0, size);
            }
        } catch (final DataFormatException e) {
            throw new RuntimeException(e);
        } finally {
            this.inflater.reset();
        }
        return data.toByteArray();
    }

}
