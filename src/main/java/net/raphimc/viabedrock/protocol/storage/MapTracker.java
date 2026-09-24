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
package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashMap;
import java.util.Map;

public class MapTracker extends StoredObject {

    private final Map<Long, Integer> javaIds = new HashMap<>();
    private int nextJavaId;

    public MapTracker(final UserConnection user) {
        super(user);
    }

    public int javaId(final long bedrockId) {
        final Integer existingId = this.javaIds.get(bedrockId);
        if (existingId != null) {
            return existingId;
        }

        final int javaId = this.nextJavaId++;
        this.javaIds.put(bedrockId, javaId);
        return javaId;
    }

    public int itemJavaId(final long bedrockId) {
        final Integer existingId = this.javaIds.get(bedrockId);
        if (existingId != null) {
            return existingId;
        }

        final int javaId = this.javaId(bedrockId);
        final PacketWrapper request = PacketWrapper.create(ServerboundBedrockPackets.MAP_INFO_REQUEST, this.user());
        request.write(BedrockTypes.VAR_LONG, bedrockId);
        request.write(BedrockTypes.UNSIGNED_INT_LE, 0L); // client pixel count
        request.sendToServer(BedrockProtocol.class);
        return javaId;
    }

}
