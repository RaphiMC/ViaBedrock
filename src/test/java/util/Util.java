/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package util;

import net.raphimc.viabedrock.api.model.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {

    private static final List<String> VANILLA_PACKS = List.of(
            "vanilla_base",
            "vanilla_music",
            "vanilla",
            "vanilla_1.14",
            "vanilla_1.15",
            "vanilla_1.16",
            "vanilla_1.16.100",
            "vanilla_1.16.200",
            "vanilla_1.16.210",
            "vanilla_1.16.220",
            "vanilla_1.17.0",
            "vanilla_1.17.10",
            "vanilla_1.17.20",
            "vanilla_1.17.30",
            "vanilla_1.17.40",
            "vanilla_1.18.0",
            "vanilla_1.18.10",
            "vanilla_1.18.20",
            "vanilla_1.18.30",
            "vanilla_1.19.0",
            "vanilla_1.19.10",
            "vanilla_1.19.20",
            "vanilla_1.19.30",
            "vanilla_1.19.40",
            "vanilla_1.19.50",
            "vanilla_1.19.60",
            "vanilla_1.19.70",
            "vanilla_1.19.80",
            "vanilla_1.20.0",
            "vanilla_1.20.10",
            "vanilla_1.20.20",
            "vanilla_1.20.30",
            "vanilla_1.20.40",
            "vanilla_1.20.50",
            "vanilla_1.20.60",
            "vanilla_1.20.70",
            "vanilla_1.20.80",
            "vanilla_1.21.0",
            "vanilla_1.21.10",
            "vanilla_1.21.20"
    );

    public static ResourcePacksStorage getClientResourcePacks(final File clientDataDir) {
        final File resourcePacksDir = new File(clientDataDir, "resource_packs");
        final long start = System.currentTimeMillis();
        final ResourcePacksStorage resourcePacksStorage = new ResourcePacksStorage(null);

        final List<UUID> packStack = new ArrayList<>();
        for (int i = VANILLA_PACKS.size() - 1; i >= 0; i--) {
            final File packDir = new File(resourcePacksDir, VANILLA_PACKS.get(i));
            if (!packDir.exists()) {
                throw new IllegalStateException("Missing vanilla pack: " + VANILLA_PACKS.get(i));
            }
            final ResourcePack resourcePack = new DirectoryResourcePack(packDir, UUID.randomUUID(), "1.0.0", "", packDir.getName(), "", false, false, false, 0, PackType.Resources);
            resourcePacksStorage.addPack(resourcePack);
            packStack.add(resourcePack.packId());
        }

        resourcePacksStorage.setPackStack(packStack.toArray(new UUID[0]), new UUID[0]);
        System.out.println("Preparation took " + (System.currentTimeMillis() - start) + "ms");
        return resourcePacksStorage;
    }

}
