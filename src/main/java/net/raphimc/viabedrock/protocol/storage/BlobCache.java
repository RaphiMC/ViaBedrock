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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.providers.BlobCacheProvider;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class BlobCache extends StoredObject {

    private final Map<Long, CompletableFuture<byte[]>> pending = new HashMap<>();

    private final Object lock = new Object();
    private final List<Long> missing = new ArrayList<>();
    private final List<Long> acked = new ArrayList<>();

    private final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
    private final Inflater inflater = new Inflater();
    private final byte[] compressionBuffer = new byte[8192];

    public BlobCache(final UserConnection user) {
        super(user);
    }

    public void tick() throws Exception {
        synchronized (this.lock) {
            if (this.missing.isEmpty() && this.acked.isEmpty()) {
                return;
            }

            final List<Long> missingSubSet = this.missing.subList(0, Math.min(4096, this.missing.size()));
            final List<Long> ackedSubSet = this.acked.subList(0, Math.min(4096, this.acked.size()));

            final PacketWrapper clientCacheBlobStatus = PacketWrapper.create(ServerboundBedrockPackets.CLIENT_CACHE_BLOB_STATUS, this.getUser());
            clientCacheBlobStatus.write(BedrockTypes.UNSIGNED_VAR_INT, missingSubSet.size()); // missing blob count
            clientCacheBlobStatus.write(BedrockTypes.UNSIGNED_VAR_INT, ackedSubSet.size()); // acked blob count
            for (long hash : missingSubSet) {
                clientCacheBlobStatus.write(BedrockTypes.LONG_LE, hash); // missing blob hash
            }
            for (long hash : ackedSubSet) {
                clientCacheBlobStatus.write(BedrockTypes.LONG_LE, hash); // acked blob hash
            }
            clientCacheBlobStatus.sendToServer(BedrockProtocol.class);

            this.missing.removeAll(missingSubSet);
            this.acked.removeAll(ackedSubSet);
        }
    }

    public void addBlob(final long hash, final byte[] blob) {
        synchronized (this.lock) {
            this.acked.add(hash);
        }

        final byte[] compressedBlob = this.compress(blob);
        final byte[] previousBlob = Via.getManager().getProviders().get(BlobCacheProvider.class).addBlob(hash, compressedBlob);
        if (this.pending.containsKey(hash)) {
            this.pending.remove(hash).complete(blob);
        }

        if (previousBlob != null && !Arrays.equals(previousBlob, compressedBlob)) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Overwriting blob with hash " + hash + "!");
        }
    }

    public boolean hasBlob(final long... hashes) {
        for (long hash : hashes) {
            if (!Via.getManager().getProviders().get(BlobCacheProvider.class).hasBlob(hash)) {
                return false;
            }
        }
        return true;
    }

    public CompletableFuture<byte[]> getBlob(final long... hashes) {
        return this.getBlob(true, hashes);
    }

    public CompletableFuture<byte[]> getBlob(final Long[] hashes) {
        final long[] longs = new long[hashes.length];
        for (int i = 0; i < hashes.length; i++) {
            longs[i] = hashes[i];
        }

        return this.getBlob(true, longs);
    }

    public CompletableFuture<byte[]> getBlob(final boolean acknowledge, final long... hashes) {
        if (acknowledge) {
            synchronized (this.lock) {
                for (long hash : hashes) {
                    if (this.hasBlob(hash)) {
                        this.acked.add(hash);
                    } else if (!this.pending.containsKey(hash)) {
                        this.missing.add(hash);
                    }
                }
            }
        }

        if (this.hasBlob(hashes)) {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                for (long hash : hashes) {
                    output.write(this.decompress(Via.getManager().getProviders().get(BlobCacheProvider.class).getBlob(hash)));
                }
            } catch (final IOException ignored) {
            }
            return CompletableFuture.completedFuture(output.toByteArray());
        }

        final CompletableFuture<byte[]> rootFuture = new CompletableFuture<>();
        for (long hash : hashes) {
            if (this.hasBlob(hash)) continue;

            CompletableFuture<byte[]> subFuture = new CompletableFuture<>();
            final CompletableFuture<byte[]> existing = this.pending.get(hash);
            if (existing != null) {
                subFuture.whenComplete((blob, throwable) -> {
                    if (throwable != null) {
                        existing.completeExceptionally(throwable);
                    } else {
                        existing.complete(blob);
                    }
                });
            }
            subFuture.whenComplete((blob, throwable) -> {
                if (throwable != null) {
                    rootFuture.completeExceptionally(throwable);
                } else if (this.hasBlob(hashes)) {
                    rootFuture.complete(this.getBlob(false, hashes).getNow(null));
                }
            });
            this.pending.put(hash, subFuture);
        }

        return rootFuture;
    }

    private byte[] compress(final byte[] data) {
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
        if (compressed.length == 0) return compressed;

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
