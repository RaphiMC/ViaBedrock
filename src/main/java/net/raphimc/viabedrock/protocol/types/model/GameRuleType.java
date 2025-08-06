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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.GameRule_Type;
import net.raphimc.viabedrock.protocol.model.GameRule;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class GameRuleType extends Type<GameRule> {

    public GameRuleType() {
        super(GameRule.class);
    }

    @Override
    public GameRule read(ByteBuf buffer) {
        final String name = BedrockTypes.STRING.read(buffer);
        final boolean editable = buffer.readBoolean();
        final GameRule_Type type = GameRule_Type.getByValue(BedrockTypes.UNSIGNED_VAR_INT.read(buffer), GameRule_Type.Invalid);
        return switch (type) {
            case Bool -> new GameRule(name, editable, buffer.readBoolean());
            case Int -> new GameRule(name, editable, BedrockTypes.UNSIGNED_VAR_INT.readPrimitive(buffer));
            case Float -> new GameRule(name, editable, BedrockTypes.FLOAT_LE.readPrimitive(buffer));
            case Invalid -> new GameRule(name, editable, null);
            default -> throw new IllegalStateException("Unhandled GameRule_Type: " + type);
        };
    }

    @Override
    public void write(ByteBuf buffer, GameRule value) {
        BedrockTypes.STRING.write(buffer, value.name());
        buffer.writeBoolean(value.editable());
        final Class<?> valueClass = value.value() == null ? null : value.value().getClass();
        final GameRule_Type type = valueClass == Boolean.class ? GameRule_Type.Bool : valueClass == Integer.class ? GameRule_Type.Int : valueClass == Float.class ? GameRule_Type.Float : GameRule_Type.Invalid;
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, type.getValue());
        switch (type) {
            case Bool -> buffer.writeBoolean((Boolean) value.value());
            case Int -> BedrockTypes.UNSIGNED_VAR_INT.writePrimitive(buffer, (Integer) value.value());
            case Float -> BedrockTypes.FLOAT_LE.writePrimitive(buffer, (Float) value.value());
            default -> throw new IllegalStateException("Unhandled GameRule_Type: " + type);
        }
    }

}
