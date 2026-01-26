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
package net.raphimc.viabedrock.api.model.entity;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.AbilitiesIndex;
import net.raphimc.viabedrock.protocol.data.enums.java.UpdateMobEffectFlag;
import net.raphimc.viabedrock.protocol.data.generated.java.Attributes;
import net.raphimc.viabedrock.protocol.data.generated.java.EntityDataFields;
import net.raphimc.viabedrock.protocol.model.EntityAttribute;
import net.raphimc.viabedrock.protocol.model.EntityEffect;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class LivingEntity extends Entity {

    protected final Map<String, EntityAttribute> attributes = new HashMap<>();
    protected final Map<String, EntityEffect> effects = new HashMap<>();

    public LivingEntity(final UserConnection user, final long uniqueId, final long runtimeId, final String type, final int javaId, final UUID javaUuid, final EntityTypes1_21_11 javaType) {
        super(user, uniqueId, runtimeId, type, javaId, javaUuid, javaType);
        this.attributes.put("minecraft:health", new EntityAttribute("minecraft:health", 20F, 0, 20F));
    }

    @Override
    public void tick() {
        super.tick();

        final Set<String> effectsToRemove = new HashSet<>();
        for (EntityEffect effect : this.effects.values()) {
            if (effect.duration().get() != -1 && effect.duration().decrementAndGet() <= 0) {
                effectsToRemove.add(effect.identifier());
            }
        }
        // Bedrock client removes effects clientside, but Java Edition doesn't, so we need to send a remove packet for each effect
        for (String identifier : effectsToRemove) {
            final PacketWrapper removeMobEffect = PacketWrapper.create(ClientboundPackets1_21_11.REMOVE_MOB_EFFECT, this.user);
            this.removeEffect(identifier, removeMobEffect);
            removeMobEffect.send(BedrockProtocol.class);
        }
    }

    public final void sendAttribute(final String name) {
        final EntityAttribute attribute = this.attributes.get(name);
        if (attribute != null) {
            this.updateAttributes(new EntityAttribute[]{attribute});
        }
    }

    public final void updateAttributes(final EntityAttribute[] attributes) {
        final PacketWrapper updateAttributes = PacketWrapper.create(ClientboundPackets1_21_11.UPDATE_ATTRIBUTES, this.user);
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
                if (attribute.computeClampedValue() <= oldAttribute.computeClampedValue()) continue;
            }
            this.attributes.put(attribute.name(), attribute);
            if (!this.translateAttribute(attribute, javaAttributes, attributeCount, javaEntityData)) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received unknown entity attribute: " + attribute.name() + " for entity type: " + this.javaType());
            }
        }
        if (attributeCount.get() > 0) {
            javaAttributes.set(Types.VAR_INT, 1, attributeCount.get());
        }
        if (!javaEntityData.isEmpty()) {
            final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_21_11.SET_ENTITY_DATA, this.user);
            setEntityData.write(Types.VAR_INT, this.javaId); // entity id
            setEntityData.write(VersionedTypes.V1_21_11.entityDataList, javaEntityData); // entity data
            setEntityData.send(BedrockProtocol.class);
        }
    }

    public final void sendEffects() {
        for (EntityEffect effect : this.effects.values()) {
            final PacketWrapper updateMobEffect = PacketWrapper.create(ClientboundPackets1_21_11.UPDATE_MOB_EFFECT, this.user);
            this.updateEffect(effect, updateMobEffect);
            updateMobEffect.send(BedrockProtocol.class);
        }
    }

    public final void updateEffect(final EntityEffect effect, final PacketWrapper javaEffect) {
        this.effects.put(effect.identifier(), effect);
        javaEffect.write(Types.VAR_INT, this.javaId); // entity id
        javaEffect.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEffects().get(BedrockProtocol.MAPPINGS.getBedrockToJavaEffects().get(effect.identifier()))); // effect id
        javaEffect.write(Types.VAR_INT, effect.amplifier()); // amplifier
        javaEffect.write(Types.VAR_INT, effect.duration().get() != -1 ? Math.max(effect.duration().get(), 0) : -1); // duration
        byte flags = 0;
        if (effect.ambient()) flags |= UpdateMobEffectFlag.AMBIENT.getBit();
        if (effect.showParticles()) flags |= UpdateMobEffectFlag.VISIBLE.getBit();
        javaEffect.write(Types.BYTE, flags); // flags
    }

    public final void removeEffect(final String identifier, final PacketWrapper javaEffect) {
        this.effects.remove(identifier);
        javaEffect.write(Types.VAR_INT, this.javaId); // entity id
        javaEffect.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEffects().get(BedrockProtocol.MAPPINGS.getBedrockToJavaEffects().get(identifier))); // effect id
    }

    public final void clearEffects() {
        this.effects.clear();
    }

    public boolean isDead() {
        return this.attributes.get("minecraft:health").computeClampedValue() <= 0F;
    }

    public void setHealth(final float health) {
        this.attributes.put("minecraft:health", this.attributes.get("minecraft:health").withValue(health));
    }

    public Map<String, EntityAttribute> attributes() {
        return this.attributes;
    }

    public Map<String, EntityEffect> effects() {
        return this.effects;
    }

    protected boolean translateAttribute(final EntityAttribute attribute, final PacketWrapper javaAttributes, final AtomicInteger attributeCount, final List<EntityData> javaEntityData) {
        return switch (attribute.name()) {
            case "minecraft:attack_damage", "minecraft:knockback_resistance", "minecraft:movement" -> {
                javaAttributes.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEntityAttributes().get(switch (attribute.name()) {
                    case "minecraft:attack_damage" -> Attributes.ATTACK_DAMAGE;
                    case "minecraft:knockback_resistance" -> Attributes.KNOCKBACK_RESISTANCE;
                    case "minecraft:movement" -> Attributes.MOVEMENT_SPEED;
                    default -> throw new IllegalStateException("Unhandled entity attribute: " + attribute.name());
                })); // attribute id
                javaAttributes.write(Types.DOUBLE, (double) attribute.computeClampedValue()); // base value
                javaAttributes.write(Types.VAR_INT, 0); // modifier count
                attributeCount.incrementAndGet();
                yield true;
            }
            case "minecraft:health" -> {
                javaEntityData.add(new EntityData(this.getJavaEntityDataIndex(EntityDataFields.HEALTH), VersionedTypes.V1_21_11.entityDataTypes.floatType, attribute.computeClampedValue()));
                javaAttributes.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getJavaEntityAttributes().get(Attributes.MAX_HEALTH)); // attribute id
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
