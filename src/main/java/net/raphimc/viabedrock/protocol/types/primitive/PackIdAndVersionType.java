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
package net.raphimc.viabedrock.protocol.types.primitive;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.UUID;
import java.util.logging.Level;

public class PackIdAndVersionType extends Type<Pair<UUID, String>> {

    public PackIdAndVersionType() {
        super("PackID and Version", Pair.class);
    }

    @Override
    public Pair<UUID, String> read(ByteBuf buffer) {
        final String packIdAndVersion = BedrockTypes.STRING.read(buffer); // id and version
        final String[] packIdAndVersionSplit = packIdAndVersion.split("_", 2);
        final String packId = packIdAndVersionSplit[0];
        final String packVersion = packIdAndVersionSplit.length > 1 ? packIdAndVersionSplit[1] : null;

        UUID packUUID;
        try {
            packUUID = UUID.fromString(packId);
        } catch (IllegalArgumentException e) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Invalid pack UUID: " + packId, e);
            packUUID = new UUID(0L, 0L);
        }

        return new Pair<>(packUUID, packVersion);
    }

    @Override
    public void write(ByteBuf buffer, Pair<UUID, String> value) {
        BedrockTypes.STRING.write(buffer, value.key().toString() + (value.value() != null ? ("_" + value.value()) : ""));
    }

}
