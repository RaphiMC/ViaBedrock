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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntLinkedOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.model.EnchantOption;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class EnchantOptionType extends Type<EnchantOption> {

    public EnchantOptionType() {
        super(EnchantOption.class);
    }

    @Override
    public EnchantOption read(final ByteBuf buffer) {
        final int cost = buffer.readUnsignedByte(); // cost
        buffer.readUnsignedIntLE(); // slot flags

        // The enchantments are split by when they are active, but all of them are shown in the same tooltip
        final Int2IntMap enchantments = new Int2IntLinkedOpenHashMap();
        for (int i = 0; i < 3; i++) {
            final int count = BedrockTypes.UNSIGNED_VAR_INT.read(buffer); // enchantment count
            for (int j = 0; j < count; j++) {
                final int id = buffer.readUnsignedByte(); // id
                final int level = buffer.readUnsignedByte(); // level
                enchantments.putIfAbsent(id, level);
            }
        }

        final String name = BedrockTypes.STRING.read(buffer); // name
        final int optionId = BedrockTypes.VAR_INT.read(buffer); // option id
        return new EnchantOption(cost, enchantments, name, optionId);
    }

    @Override
    public void write(final ByteBuf buffer, final EnchantOption value) {
        throw new UnsupportedOperationException("Writing enchant options is not supported");
    }

}
