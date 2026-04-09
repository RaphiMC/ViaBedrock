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
package net.raphimc.viabedrock.protocol.data;

import net.raphimc.viabedrock.api.resourcepack.ResourcePack;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

public class DataValues {

    public static final ResourcePack.Key VANILLA_RESOURCE_PACK_KEY = ResourcePack.Key.fromString("0575c61f-a5da-4b7f-9961-ffda2908861e_0.0.1");
    public static final ResourcePack.Key VANILLA_SKIN_PACK_KEY = ResourcePack.Key.fromString("c18e65aa-7b21-4637-9b63-8ad63622ef01_1.0.0");

    public static void validate() {
        assert BedrockProtocol.MAPPINGS.getBedrockResourcePacks().containsKey(VANILLA_RESOURCE_PACK_KEY);
        assert BedrockProtocol.MAPPINGS.getBedrockSkinPacks().containsKey(VANILLA_SKIN_PACK_KEY);
    }

}
