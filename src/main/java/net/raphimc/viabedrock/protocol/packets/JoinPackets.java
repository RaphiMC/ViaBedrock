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
package net.raphimc.viabedrock.protocol.packets;

import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class JoinPackets {

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.START_GAME, ClientboundPackets1_19_3.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.cancel();
                    wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
                    final long runtimeId = wrapper.read(BedrockTypes.UNSIGNED_VAR_LONG); // runtime entity id
                    final int gamemode = wrapper.read(BedrockTypes.VAR_INT); // player game type
                    wrapper.read(BedrockTypes.POSITION_3F); // player position
                    wrapper.read(BedrockTypes.POSITION_2F); // rotation

                    // Level settings
                    wrapper.read(BedrockTypes.LONG_LE); // seed
                    wrapper.read(BedrockTypes.SHORT_LE); // spawn biome type
                    wrapper.read(BedrockTypes.STRING); // custom biome name
                    wrapper.read(BedrockTypes.VAR_INT); // dimension id
                    wrapper.read(BedrockTypes.VAR_INT); // generator id
                    wrapper.read(BedrockTypes.VAR_INT); // level game type
                    wrapper.read(BedrockTypes.VAR_INT); // difficulty
                    wrapper.read(BedrockTypes.POSITION_3I); // default spawn position
                    wrapper.read(Type.BOOLEAN); // achievements disabled
                    wrapper.read(Type.BOOLEAN); // world editor
                    wrapper.read(BedrockTypes.VAR_INT); // day cycle stop time
                    wrapper.read(BedrockTypes.VAR_INT); // education edition offers
                    wrapper.read(Type.BOOLEAN); // education features enabled
                    wrapper.read(BedrockTypes.STRING); // education product id
                    wrapper.read(BedrockTypes.FLOAT_LE); // rain level
                    wrapper.read(BedrockTypes.FLOAT_LE); // lightning level
                    wrapper.read(Type.BOOLEAN); // platform locked content confirmed
                    wrapper.read(Type.BOOLEAN); // multiplayer game
                    wrapper.read(Type.BOOLEAN); // is broadcasting to lan
                    wrapper.read(BedrockTypes.VAR_INT); // Xbox Live broadcast mode
                    wrapper.read(BedrockTypes.VAR_INT); // platform broadcast mode
                    wrapper.read(Type.BOOLEAN); // commands enabled
                    wrapper.read(Type.BOOLEAN); // texture packs required
                    wrapper.read(BedrockTypes.GAME_RULE_ARRAY); // game rules
                    wrapper.read(BedrockTypes.EXPERIMENT_ARRAY); // experiments
                    wrapper.read(Type.BOOLEAN); // experiments previously toggled
                    wrapper.read(Type.BOOLEAN); // bonus chest enabled
                    wrapper.read(Type.BOOLEAN); // start with map enabled
                    wrapper.read(BedrockTypes.VAR_INT); // player permission
                    wrapper.read(BedrockTypes.INT_LE); // server chunk tick range
                    wrapper.read(Type.BOOLEAN); // behavior pack locked
                    wrapper.read(Type.BOOLEAN); // resource pack locked
                    wrapper.read(Type.BOOLEAN); // from locked world template
                    wrapper.read(Type.BOOLEAN); // using msa gamer tags only
                    wrapper.read(Type.BOOLEAN); // from world template
                    wrapper.read(Type.BOOLEAN); // world template option locked
                    wrapper.read(Type.BOOLEAN); // only spawn v1 villagers
                    wrapper.read(Type.BOOLEAN); // disable personas
                    wrapper.read(Type.BOOLEAN); // disable custom skins
                    wrapper.read(BedrockTypes.STRING); // vanilla version
                    wrapper.read(BedrockTypes.INT_LE); // limited world width
                    wrapper.read(BedrockTypes.INT_LE); // limited world height
                    wrapper.read(Type.BOOLEAN); // nether type
                    wrapper.read(BedrockTypes.EDUCATION_URI_RESOURCE); // education shared uri
                    if (wrapper.read(Type.BOOLEAN)) {
                        wrapper.read(Type.BOOLEAN); // force experimental game play
                    }
                    wrapper.read(Type.BYTE); // chat restriction level
                    wrapper.read(Type.BOOLEAN); // disabling player interactions

                    // Continue reading start game packet
                    wrapper.read(BedrockTypes.STRING); // level id
                    wrapper.read(BedrockTypes.STRING); // level name
                    wrapper.read(BedrockTypes.STRING); // premium world template id
                    wrapper.read(BedrockTypes.VAR_INT); // movement mode
                    wrapper.read(BedrockTypes.VAR_INT); // rewind history size
                    wrapper.read(Type.BOOLEAN); // is trial
                    wrapper.read(Type.BOOLEAN); // server authoritative block breaking
                    wrapper.read(BedrockTypes.LONG_LE); // current tick
                    wrapper.read(BedrockTypes.VAR_INT); // enchantment seed
                    wrapper.read(BedrockTypes.BLOCK_PROPERTIES_ARRAY); // block properties
                    wrapper.read(BedrockTypes.ITEM_ENTRY_ARRAY); // item entries
                    wrapper.read(BedrockTypes.STRING); // multiplayer correlation id
                    wrapper.read(Type.BOOLEAN); // inventories server authoritative
                    wrapper.read(BedrockTypes.STRING); // server engine
                    wrapper.read(BedrockTypes.TAG); // player property data
                    wrapper.read(BedrockTypes.LONG_LE); // block registry checksum
                    wrapper.read(BedrockTypes.UUID); // world template id
                    wrapper.read(Type.BOOLEAN); // client side generation
                });
            }
        });
    }

}
