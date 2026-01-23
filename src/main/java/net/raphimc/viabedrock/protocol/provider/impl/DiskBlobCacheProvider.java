/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.protocol.provider.impl;

import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.io.BlobDB;
import net.raphimc.viabedrock.protocol.provider.BlobCacheProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.stream.Stream;

public class DiskBlobCacheProvider extends BlobCacheProvider {

    private static BlobDB BLOB_DB;

    public DiskBlobCacheProvider() {
        if (BLOB_DB != null) return;

        try {
            try {
                BLOB_DB = new BlobDB(ViaBedrock.getPlatform().getBlobCacheFolder());
            } catch (Throwable e) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to open BlobDB, deleting it...", e);
                try (Stream<Path> paths = Files.walk(ViaBedrock.getPlatform().getBlobCacheFolder().toPath())) {
                    for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(path);
                    }
                }
                BLOB_DB = new BlobDB(ViaBedrock.getPlatform().getBlobCacheFolder());
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    BLOB_DB.save();
                    BLOB_DB.close();
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to close blob cache", e);
                }
            }));
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to open or create blob cache", e);
        }
    }

    @Override
    public void addBlob(final long hash, final byte[] blob) {
        BLOB_DB.queuePut(hash, blob);
    }

    @Override
    public boolean hasBlob(final long hash) {
        return BLOB_DB.contains(hash);
    }

    @Override
    public byte[] getBlob(final long hash) {
        try {
            return BLOB_DB.get(hash);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
