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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.model.AttributeInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LivingEntity extends Entity {

    protected Map<String, AttributeInstance> attributes = new HashMap<>();

    public LivingEntity(final UserConnection user, final long uniqueId, final long runtimeId, final int javaId, final UUID javaUuid, final EntityTypes1_20_5 type) {
        super(user, uniqueId, runtimeId, javaId, javaUuid, type);
    }

    public void updateAttributes(final AttributeInstance[] attributes) {
        final PacketWrapper updateAttributes = PacketWrapper.create(ClientboundPackets1_21.UPDATE_ATTRIBUTES, this.user);
        this.updateAttributes(attributes, updateAttributes);
        updateAttributes.send(BedrockProtocol.class);
    }

    public void updateAttributes(final AttributeInstance[] attributes, final PacketWrapper javaAttributes) {
        javaAttributes.write(Types.VAR_INT, this.javaId); // entity id
        javaAttributes.write(Types.VAR_INT, 0); // attribute count
        for (AttributeInstance attributeInstance : attributes) {
            this.attributes.put(attributeInstance.name(), attributeInstance);
        }
    }

    public Map<String, AttributeInstance> attributes() {
        return this.attributes;
    }

}
