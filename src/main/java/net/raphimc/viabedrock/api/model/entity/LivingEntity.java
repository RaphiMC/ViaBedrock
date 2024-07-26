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
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.AbilitiesIndex;
import net.raphimc.viabedrock.protocol.model.EntityAttribute;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class LivingEntity extends Entity {

    protected Map<String, EntityAttribute> attributes = new HashMap<>();

    public LivingEntity(final UserConnection user, final long uniqueId, final long runtimeId, final int javaId, final UUID javaUuid, final EntityTypes1_20_5 type) {
        super(user, uniqueId, runtimeId, javaId, javaUuid, type);
        this.attributes.put("minecraft:health", new EntityAttribute("minecraft:health", 20F, 0, 20F));
    }

    public final void sendAttribute(final String name) {
        final EntityAttribute attribute = this.attributes.get(name);
        if (attribute != null) {
            this.updateAttributes(new EntityAttribute[]{attribute});
        }
    }

    public final void updateAttributes(final EntityAttribute[] attributes) {
        final PacketWrapper updateAttributes = PacketWrapper.create(ClientboundPackets1_21.UPDATE_ATTRIBUTES, this.user);
        this.updateAttributes(attributes, updateAttributes);
        updateAttributes.send(BedrockProtocol.class);
    }

    public final void updateAttributes(final EntityAttribute[] attributes, final PacketWrapper javaAttributes) {
        javaAttributes.write(Types.VAR_INT, this.javaId); // entity id
        javaAttributes.write(Types.VAR_INT, 0); // attribute count
        final AtomicInteger attributeCount = new AtomicInteger(0);
        final List<EntityData> javaEntityData = new ArrayList<>();
        for (EntityAttribute attribute : attributes) {
            if (attribute.name().equals("minecraft:health") && this instanceof PlayerEntity player && player.abilities().getBooleanValue(AbilitiesIndex.Invulnerable)) {
                final EntityAttribute oldAttribute = this.attributes.get(attribute.name());
                if (attribute.computeValue(false) <= oldAttribute.computeValue(false)) continue;
            }
            this.attributes.put(attribute.name(), attribute);
            if (!this.translateAttribute(attribute, javaAttributes, attributeCount, javaEntityData)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown entity attribute: " + attribute.name() + " for entity type: " + this.type());
            }
        }
        if (attributeCount.get() > 0) {
            javaAttributes.set(Types.VAR_INT, 1, attributeCount.get());
        }
        if (!javaEntityData.isEmpty()) {
            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21.SET_ENTITY_DATA, this.user);
            setEntityData.write(Types.VAR_INT, this.javaId); // entity id
            setEntityData.write(Types1_21.ENTITY_DATA_LIST, javaEntityData); // entity data
            setEntityData.send(BedrockProtocol.class);
        }
    }

    public boolean isDead() {
        return this.attributes.get("minecraft:health").computeValue(false) <= 0F;
    }

    public void setHealth(final float health) {
        this.attributes.put("minecraft:health", this.attributes.get("minecraft:health").withValue(health));
    }

    public Map<String, EntityAttribute> attributes() {
        return this.attributes;
    }

    protected boolean translateAttribute(final EntityAttribute attribute, final PacketWrapper javaAttributes, final AtomicInteger attributeCount, final List<EntityData> javaEntityData) {
        return switch (attribute.name()) {
            case "minecraft:attack_damage", "minecraft:knockback_resistance", "minecraft:movement" -> {
                javaAttributes.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEntityAttributes().get(switch (attribute.name()) {
                    case "minecraft:attack_damage" -> "minecraft:generic.attack_damage";
                    case "minecraft:knockback_resistance" -> "minecraft:generic.knockback_resistance";
                    case "minecraft:movement" -> "minecraft:generic.movement_speed";
                    default -> throw new IllegalStateException("Unhandled entity attribute: " + attribute.name());
                })); // attribute id
                javaAttributes.write(Types.DOUBLE, (double) attribute.computeValue(true)); // base value
                javaAttributes.write(Types.VAR_INT, 0); // modifier count
                attributeCount.incrementAndGet();
                yield true;
            }
            case "minecraft:health" -> {
                javaEntityData.add(new EntityData(this.getJavaEntityDataIndex("HEALTH"), Types1_21.ENTITY_DATA_TYPES.floatType, attribute.computeValue(false)));
                javaAttributes.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEntityAttributes().get("minecraft:generic.max_health")); // attribute id
                javaAttributes.write(Types.DOUBLE, (double) attribute.maxValue()); // base value
                javaAttributes.write(Types.VAR_INT, 0); // modifier count
                attributeCount.incrementAndGet();
                yield true;
            }
            case "minecraft:absorption", "minecraft:follow_range", "minecraft:luck" -> true; // Ignore for generic entities
            case "minecraft:lava_movement", "minecraft:underwater_movement" -> true; // Ignore for now because Java Edition doesn't have these attributes
            default -> false;
        };
    }

}
