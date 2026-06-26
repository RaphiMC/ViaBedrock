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

import net.raphimc.viabedrock.api.resourcepack.ResourcePack;
import net.raphimc.viabedrock.api.resourcepack.content.ZipContent;
import net.raphimc.viabedrock.protocol.provider.ResourcePackProvider;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryResourcePackProvider extends ResourcePackProvider {

    private final Map<String, byte[]> resourcePacks = new ConcurrentHashMap<>();

    @Override
    public boolean has(final ResourcePack.Key key) {
        return this.resourcePacks.containsKey(key.toString());
    }

    @Override
    public ResourcePack load(final ResourcePack.Key key) throws IOException {
        if (!this.has(key)) {
            throw new IOException("Pack not found");
        }
        return new ResourcePack(new ZipContent(this.resourcePacks.get(key.toString())));
    }

    @Override
    public void save(final ResourcePack resourcePack) throws IOException {
        this.resourcePacks.put(resourcePack.key().toString(), resourcePack.content().toZip());
    }

}
