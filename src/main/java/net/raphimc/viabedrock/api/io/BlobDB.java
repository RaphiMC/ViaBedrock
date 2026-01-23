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
package net.raphimc.viabedrock.api.io;

import com.viaversion.viaversion.api.Via;
import net.raphimc.viabedrock.api.util.LZ4;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BlobDB implements Closeable {

    private static final byte[] MAGIC = new byte[]{'B', 'D', 'B'};
    private static final int VERSION = 1;

    private final File indexFile;
    private final RandomAccessFile dataFile;
    private final Map<Long, IndexEntry> index = new LinkedHashMap<>();
    private final Map<Long, byte[]> pendingWrites = new ConcurrentHashMap<>();
    private final Thread writeThread;
    private boolean indexDirty = false;
    private long dataOffset = 0;

    public BlobDB(final File directory) throws IOException {
        directory.mkdirs();
        this.indexFile = new File(directory, "index.bdbi");
        this.dataFile = new RandomAccessFile(new File(directory, "data.bdbd"), "rw");
        try {
            this.load();
        } catch (Throwable e) {
            this.dataFile.close();
            throw e;
        }
        this.writeThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1000);
                    final Set<Long> writtenKeys = new HashSet<>();
                    for (Map.Entry<Long, byte[]> entry : this.pendingWrites.entrySet()) {
                        try {
                            this.putNow(entry.getKey(), entry.getValue());
                            writtenKeys.add(entry.getKey());
                        } catch (Throwable e) {
                            Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to write pending blob", e);
                            break;
                        }
                    }
                    writtenKeys.forEach(this.pendingWrites::remove);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "BlobDB Write Thread");
        this.writeThread.start();
    }

    public synchronized void save() throws IOException {
        if (!this.indexDirty) return;

        this.waitForWrites();
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.indexFile)))) {
            dos.write(MAGIC);
            dos.writeInt(VERSION);
            for (final Map.Entry<Long, IndexEntry> entry : this.index.entrySet()) {
                dos.writeLong(entry.getKey());
                dos.writeInt(entry.getValue().length);
            }
        }
        this.indexDirty = false;
    }

    public synchronized boolean contains(final long key) {
        if (this.pendingWrites.containsKey(key)) {
            return true;
        }
        return this.index.containsKey(key);
    }

    public synchronized byte[] get(final long key) throws IOException {
        final byte[] pending = this.pendingWrites.get(key);
        if (pending != null) {
            return pending;
        }

        final IndexEntry entry = this.index.get(key);
        if (entry == null) {
            return null;
        }
        this.dataFile.seek(entry.offset);
        final byte[] value = new byte[entry.length];
        this.dataFile.readFully(value);
        return LZ4.decompress(value);
    }

    public synchronized void queuePut(final long key, final byte[] value) {
        if (this.index.containsKey(key)) {
            throw new IllegalArgumentException("Key already exists: " + key);
        }

        this.pendingWrites.put(key, value);
    }

    public synchronized void putNow(final long key, final byte[] value) throws IOException {
        if (this.index.containsKey(key)) {
            throw new IllegalArgumentException("Key already exists: " + key);
        }

        final byte[] compressedValue = LZ4.compress(value);
        this.dataFile.seek(this.dataOffset);
        this.dataFile.write(compressedValue);
        this.index.put(key, new IndexEntry(this.dataOffset, compressedValue.length));
        this.dataOffset += compressedValue.length;
        this.indexDirty = true;
    }

    public void waitForWrites() {
        while (!this.pendingWrites.isEmpty()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        this.writeThread.interrupt();
        this.dataFile.close();
    }

    private void load() throws IOException {
        if (!this.indexFile.exists()) return;

        long availableBytes = this.indexFile.length();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(this.indexFile)))) {
            final byte[] magic = new byte[MAGIC.length];
            dis.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException("Wrong magic: " + Arrays.toString(magic));
            }
            availableBytes -= magic.length;
            final int version = dis.readInt();
            if (version != VERSION) {
                throw new IOException("Wrong version: " + version);
            }
            availableBytes -= Integer.BYTES;
            while (availableBytes > 0) {
                final long key = dis.readLong();
                final int length = dis.readInt();
                availableBytes -= Long.BYTES + Integer.BYTES;
                this.index.put(key, new IndexEntry(this.dataOffset, length));
                this.dataOffset += length;
            }
        }
    }

    private record IndexEntry(long offset, int length) {
    }

}
