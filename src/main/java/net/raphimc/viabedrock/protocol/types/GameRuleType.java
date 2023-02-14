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
package net.raphimc.viabedrock.protocol.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.model.GameRule;

import java.util.logging.Level;

public class GameRuleType extends Type<GameRule<?>> {

    public GameRuleType() {
        super(GameRule.class);
    }

    @Override
    public GameRule<?> read(ByteBuf buffer) throws Exception {
        final String name = BedrockTypes.STRING.read(buffer);
        final boolean editable = buffer.readBoolean();
        final int type = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        switch (type) {
            case 1:
                return new GameRule<>(name, editable, buffer.readBoolean());
            case 2:
                return new GameRule<>(name, editable, BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer));
            case 3:
                return new GameRule<>(name, editable, BedrockTypes.FLOAT_LE.readPrimitive(buffer));
            default:
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown game rule type: " + type);
                return new GameRule<>(name, editable, null);
        }

    }

    @Override
    public void write(ByteBuf buffer, GameRule value) throws Exception {
        BedrockTypes.STRING.write(buffer, value.name());
        buffer.writeBoolean(value.editable());
        final Class<?> type = value.value() == null ? null : value.value().getClass();
        final int typeId = type == Boolean.class ? 1 : type == Integer.class ? 2 : type == Float.class ? 3 : -1;
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, typeId);
        switch (typeId) {
            case 1:
                buffer.writeBoolean((Boolean) value.value());
                break;
            case 2:
                BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, (Integer) value.value());
                break;
            case 3:
                BedrockTypes.FLOAT_LE.writePrimitive(buffer, (Float) value.value());
                break;
        }
    }

}
