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
package net.raphimc.viabedrock.api.io;

import net.raphimc.viabedrock.ViaBedrock;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.leveldb.util.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LevelDB implements Closeable {

    private final DB db;

    public LevelDB(final File path) throws IOException {
        final Options options = new Options().createIfMissing(true).verifyChecksums(true).maxOpenFiles(50).compressionType(CompressionType.NONE);

        DB db = null;
        try {
            db = Iq80DBFactory.factory.open(path, options);
            db.iterator().close();
        } catch (Throwable e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to open LevelDB, deleting it...", e);
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Throwable ignored) {
            }
            FileUtils.deleteRecursively(path);
            db = Iq80DBFactory.factory.open(path, options);
        }

        this.db = db;
    }

    public byte[] get(final byte[] key) {
        return this.db.get(key);
    }

    public void put(final byte[] key, final byte[] value) {
        this.db.put(key, value);
    }

    @Override
    public void close() throws IOException {
        this.db.close();
    }

}
