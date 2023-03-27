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

import com.viaversion.viaversion.api.platform.providers.Provider;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.model.ResourcePack;

import java.io.File;

public abstract class ResourcePackProvider implements Provider {

    public abstract boolean hasPack(final ResourcePack pack) throws Exception;

    public abstract void loadPack(final ResourcePack pack) throws Exception;

    public abstract void addPack(final ResourcePack pack) throws Exception;

    public File getPackFile(final ResourcePack pack) {
        return new File(ViaBedrock.getPlatform().getServerPacksFolder(), pack.packId() + "_" + pack.version() + ".mcpack");
    }

}
