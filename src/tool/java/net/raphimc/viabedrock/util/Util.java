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
package net.raphimc.viabedrock.util;

import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PackType;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {

    public static ResourcePacksStorage getClientResourcePacks(final File clientDataDir) {
        final File resourcePacksDir = new File(clientDataDir, "resource_packs");
        final long start = System.currentTimeMillis();
        final ResourcePacksStorage resourcePacksStorage = new ResourcePacksStorage(null);

        final List<UUID> packStack = new ArrayList<>();
        for (int i = ResourcePacksStorage.VANILLA_PACK_NAMES.size() - 1; i >= 0; i--) {
            final File packDir = new File(resourcePacksDir, ResourcePacksStorage.VANILLA_PACK_NAMES.get(i));
            if (!packDir.exists()) {
                throw new IllegalStateException("Missing vanilla pack: " + ResourcePacksStorage.VANILLA_PACK_NAMES.get(i));
            }
            final ResourcePack resourcePack = new DirectoryResourcePack(packDir, UUID.randomUUID(), "1.0.0", new byte[0], packDir.getName(), "", false, false, false, 0, PackType.Resources);
            resourcePacksStorage.addPack(resourcePack);
            packStack.add(resourcePack.packId());
        }

        resourcePacksStorage.setPackStack(packStack.toArray(new UUID[0]));
        System.out.println("Preparation took " + (System.currentTimeMillis() - start) + "ms");
        return resourcePacksStorage;
    }

}
