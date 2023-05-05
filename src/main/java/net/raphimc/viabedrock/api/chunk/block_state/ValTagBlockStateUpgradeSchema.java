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
package net.raphimc.viabedrock.api.chunk.block_state;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.ShortTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.protocol.BedrockProtocol;

import java.util.logging.Level;

public class ValTagBlockStateUpgradeSchema extends BlockStateUpgradeSchema {

    public ValTagBlockStateUpgradeSchema() {
        super(1);

        this.actions.add(tag -> {
            if (tag.get("val") instanceof ShortTag) {
                final String name = tag.<StringTag>get("name").getValue();
                if (!BedrockProtocol.MAPPINGS.getLegacyBlocks().containsKey(name)) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block " + name + " in val tag block state upgrade schema");
                    return;
                }
                final int id = BedrockProtocol.MAPPINGS.getLegacyBlocks().get(name);

                final short metadata = tag.<ShortTag>remove("val").asShort();
                if (metadata < 0 || metadata > 63) return;

                BedrockBlockState blockState = BedrockProtocol.MAPPINGS.getLegacyBlockStates().get(id << 6 | metadata & 63);
                if (blockState == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state " + name + ":" + metadata + " in val tag block state upgrade schema");
                    blockState = BedrockProtocol.MAPPINGS.getLegacyBlockStates().get(id << 6);
                }

                tag.put("states", blockState.blockStateTag().get("states").clone());

                throw StopUpgrade.INSTANCE;
            }
        });
    }

}
