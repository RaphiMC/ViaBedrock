/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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

import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.provider.ResourcePackProvider;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryResourcePackProvider extends ResourcePackProvider {

    private final Map<String, byte[]> packs = new ConcurrentHashMap<>();

    @Override
    public boolean hasPack(final ResourcePack pack) {
        return this.packs.containsKey(this.getPackKey(pack));
    }

    @Override
    public void loadPack(final ResourcePack pack) throws Exception {
        if (!this.hasPack(pack)) {
            throw new IOException("Pack not found");
        }
        final byte[] data = this.packs.get(this.getPackKey(pack));

        pack.setContentKey(new byte[0]);
        pack.setCompressedDataLength(data.length, data.length);
        pack.processDataChunk(0, data);
    }

    @Override
    public void addPack(final ResourcePack pack) throws IOException {
        this.packs.put(this.getPackKey(pack), pack.content().toZip());
    }

    private String getPackKey(final ResourcePack pack) {
        return pack.packId() + "_" + pack.version();
    }

}
