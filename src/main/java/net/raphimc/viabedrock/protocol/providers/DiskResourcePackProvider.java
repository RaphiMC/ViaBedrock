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

import net.raphimc.viabedrock.protocol.model.ResourcePack;

import java.io.IOException;
import java.nio.file.Files;

public class DiskResourcePackProvider extends ResourcePackProvider {

    @Override
    public boolean hasPack(final ResourcePack pack) {
        return this.getPackFile(pack).isFile();
    }

    @Override
    public void loadPack(final ResourcePack pack) throws Exception {
        if (!this.hasPack(pack)) {
            throw new IOException("Pack not found");
        }
        final byte[] data = Files.readAllBytes(this.getPackFile(pack).toPath());

        pack.setContentKey("");
        pack.setCompressedDataLength(data.length, data.length);
        pack.processDataChunk(0, data);
    }

    @Override
    public void addPack(final ResourcePack pack) throws IOException {
        Files.write(this.getPackFile(pack).toPath(), pack.content().toZip());
    }

}
