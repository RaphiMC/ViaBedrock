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
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.CommandOriginType;
import net.raphimc.viabedrock.protocol.model.CommandOriginData;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Locale;
import java.util.UUID;

public class CommandOriginDataType extends Type<CommandOriginData> {

    public CommandOriginDataType() {
        super(CommandOriginData.class);
    }

    @Override
    public CommandOriginData read(ByteBuf buffer) {
        final String rawType = BedrockTypes.STRING.read(buffer);
        final CommandOriginType type = CommandOriginType.getByValue(rawType);
        if (type == null) { // Bedrock client disconnects if the type is not valid
            throw new IllegalStateException("Unknown CommandOriginType: " + rawType);
        }

        final UUID uuid = BedrockTypes.UUID.read(buffer);
        final String requestId = BedrockTypes.STRING.read(buffer);
        final long uniquePlayerId = buffer.readLongLE();

        return new CommandOriginData(type, uuid, requestId, uniquePlayerId);
    }

    @Override
    public void write(ByteBuf buffer, CommandOriginData value) {
        BedrockTypes.STRING.write(buffer, value.type().name().toLowerCase(Locale.ROOT));
        BedrockTypes.UUID.write(buffer, value.uuid());
        BedrockTypes.STRING.write(buffer, value.requestId());
        buffer.writeLongLE(value.uniquePlayerId());
    }

}
