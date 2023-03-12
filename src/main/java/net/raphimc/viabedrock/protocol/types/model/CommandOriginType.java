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
package net.raphimc.viabedrock.protocol.types.model;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.CommandOriginTypes;
import net.raphimc.viabedrock.protocol.model.CommandOrigin;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class CommandOriginType extends Type<CommandOrigin> {

    public CommandOriginType() {
        super(CommandOrigin.class);
    }

    @Override
    public CommandOrigin read(ByteBuf buffer) throws Exception {
        final int type = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        final java.util.UUID uuid = BedrockTypes.UUID.read(buffer);
        final String requestId = BedrockTypes.STRING.read(buffer);

        long event = -1;
        if (type == CommandOriginTypes.DEV_CONSOLE || type == CommandOriginTypes.TEST) {
            event = BedrockTypes.VAR_LONG.read(buffer);
        }

        return new CommandOrigin(type, uuid, requestId, event);
    }

    @Override
    public void write(ByteBuf buffer, CommandOrigin value) throws Exception {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.type());
        BedrockTypes.UUID.write(buffer, value.uuid());
        BedrockTypes.STRING.write(buffer, value.requestId());

        if (value.type() == CommandOriginTypes.DEV_CONSOLE || value.type() == CommandOriginTypes.TEST) {
            BedrockTypes.VAR_LONG.write(buffer, value.event());
        }
    }

}
