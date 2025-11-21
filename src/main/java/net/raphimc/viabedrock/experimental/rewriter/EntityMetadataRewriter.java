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
package net.raphimc.viabedrock.experimental.rewriter;

import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorFlags;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class EntityMetadataRewriter {

    // Called in Entity#translateEntityData if experimental features are enabled
    public static boolean rewrite(final Entity entity, final ActorDataIDs id, final EntityData entityData, final List<EntityData> javaEntityData) {

        switch (id) {
            case RESERVED_0, RESERVED_092 -> { // Entity flags mask
                Set<ActorFlags> bedrockFlags = entity.entityFlags();
                byte javaBitMask = 0; // https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Entity
                if (bedrockFlags.contains(ActorFlags.ONFIRE)) {
                    javaBitMask |= (1 << 0);
                }
                if (bedrockFlags.contains(ActorFlags.SNEAKING)) {
                    javaBitMask |= (1 << 1);
                }
                if (bedrockFlags.contains(ActorFlags.RIDING)) {
                    javaBitMask |= (1 << 2);
                }
                if (bedrockFlags.contains(ActorFlags.SPRINTING)) {
                    javaBitMask |= (1 << 3);
                }
                if (bedrockFlags.contains(ActorFlags.SWIMMING)) {
                    javaBitMask |= (1 << 4);
                }
                if (bedrockFlags.contains(ActorFlags.INVISIBLE)) {
                    javaBitMask |= (1 << 5);
                }

                //TODO: Handle the other flags properly?
                //ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unhandled movement bitmask bits for entity " + entity.type() + ": " + Long.toBinaryString(remaining));

                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("SHARED_FLAGS"), VersionedTypes.V1_21_9.entityDataTypes().byteType, javaBitMask));

                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("SILENT"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, bedrockFlags.contains(ActorFlags.SILENT)));
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("NO_GRAVITY"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, !bedrockFlags.contains(ActorFlags.HAS_GRAVITY)));

                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.MOB)) {
                    byte mobBitMask = 0;
                    if (bedrockFlags.contains(ActorFlags.NOAI)) {
                        mobBitMask |= 0x01;
                    }

                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("MOB_FLAGS"), VersionedTypes.V1_21_9.entityDataTypes().byteType, mobBitMask));
                }

                if (entity.javaType().is(EntityTypes1_21_9.ALLAY)) {
                    boolean dancing = bedrockFlags.contains(ActorFlags.DANCING);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("DANCING"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, dancing));
                }

                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.ABSTRACT_AGEABLE)) {
                    boolean isBaby = bedrockFlags.contains(ActorFlags.BABY);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("BABY"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isBaby));
                }

                if (entity.javaType().is(EntityTypes1_21_9.AXOLOTL)) {
                    boolean playingDead = bedrockFlags.contains(ActorFlags.PLAYING_DEAD);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("PLAYING_DEAD"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, playingDead));
                }

                if (entity.javaType().is(EntityTypes1_21_9.BEE)) {
                    byte beeBitMask = 0;
                    if (bedrockFlags.contains(ActorFlags.ANGRY)) {
                        beeBitMask |= 0x02;
                    }

                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("FLAGS"), VersionedTypes.V1_21_9.entityDataTypes().byteType, beeBitMask));
                }

                if (entity.javaType().is(EntityTypes1_21_9.OCELOT)) {
                    boolean isTrusting = bedrockFlags.contains(ActorFlags.TRUSTING);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TRUSTING"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isTrusting));
                }

                if (entity.javaType().is(EntityTypes1_21_9.SHEEP)) {
                    byte sheepBitMask = 0;
                    if (bedrockFlags.contains(ActorFlags.SHEARED)) {
                        sheepBitMask |= 0x10;
                    }
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("WOOL"), VersionedTypes.V1_21_9.entityDataTypes().byteType, sheepBitMask));
                }

                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.TAMABLE_ANIMAL)) {
                    byte tamableBitMask = 0;
                    if (bedrockFlags.contains(ActorFlags.SITTING)) {
                        tamableBitMask |= 0x01;
                    }
                    if (bedrockFlags.contains(ActorFlags.TAMED)) {
                        tamableBitMask |= 0x04;
                    }

                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("FLAGS"), VersionedTypes.V1_21_9.entityDataTypes().byteType, tamableBitMask));
                }

                if (entity.javaType().is(EntityTypes1_21_9.BOGGED)) {
                    boolean isSheared = bedrockFlags.contains(ActorFlags.SHEARED);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("SHEARED"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isSheared));
                }

                if (entity.javaType().is(EntityTypes1_21_9.CREEPER)) {
                    boolean charged = bedrockFlags.contains(ActorFlags.CHARGED);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("IS_POWERED"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, charged));

                    boolean ignited = bedrockFlags.contains(ActorFlags.IGNITED);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("IS_IGNITED"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, ignited));
                }

                if (entity.javaType().is(EntityTypes1_21_9.ZOGLIN)) {
                    boolean isBaby = bedrockFlags.contains(ActorFlags.BABY);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("BABY"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isBaby));
                }

                if (entity.javaType().is(EntityTypes1_21_9.ZOMBIE)) {
                    boolean isBaby = bedrockFlags.contains(ActorFlags.BABY);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("BABY"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isBaby));
                }

                if (entity.javaType().is(EntityTypes1_21_9.PIGLIN)) {
                    boolean isBaby = bedrockFlags.contains(ActorFlags.BABY);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("BABY"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isBaby));

                    boolean isDancing = bedrockFlags.contains(ActorFlags.DANCING);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("IS_DANCING"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isDancing));
                }

                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.ABSTRACT_RAIDER)) {
                    boolean isCelebrating = bedrockFlags.contains(ActorFlags.CELEBRATING);
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("IS_CELEBRATING"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isCelebrating));
                }


            }
            case VARIANT -> {
                int variant = (int) entityData.getValue();

                switch (entity.javaType()) {
                    case WOLF -> {
                        int javaVariant = switch (variant) {
                            case 0 -> 4; // PALE
                            case 1 -> 7; // ASHEN
                            case 2 -> 6; // BLACK
                            case 3 -> 2; // CHESTNUT
                            case 4 -> 1; // RUSTY
                            case 5 -> 8; // SNOWY
                            case 6 -> 0; // SPOTTED
                            case 7 -> 3; // STRIPED
                            case 8 -> 5; // WOODS
                            default -> {
                                ViaBedrock.getPlatform().getLogger().warning("Unknown wolf variant " + variant + " for entity " + entity.type() + ", defaulting to PALE.");
                                yield 4;
                            }
                        };
                        javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("VARIANT"), VersionedTypes.V1_21_9.entityDataTypes().wolfVariantType, javaVariant));
                    }
                    case HORSE -> {
                        javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TYPE_VARIANT"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, variant));
                    }
                    case FROG -> {
                        int javaVariant = switch (variant) {
                            case 0 -> 1; // TEMPERATE
                            case 1 -> 2; // COLD
                            case 2 -> 0; // WARM
                            default -> {
                                ViaBedrock.getPlatform().getLogger().warning("Unknown frog variant " + variant + " for entity " + entity.type() + ", defaulting to TEMPERATE.");
                                yield 1;
                            }
                        };
                        javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("VARIANT"), VersionedTypes.V1_21_9.entityDataTypes().frogVariantType, javaVariant));
                    }
                    case TROPICAL_FISH -> {
                        //TODO: Remap tropical fish variants properly
                        //javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TYPE_VARIANT"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, variant));
                    }
                    case PUFFERFISH -> {} // For some reason bedrock sends the puffed state here as well as in the PUFFED_STATE Actor ID so we ignore this one
                    default -> {
                        if (variant != 0) { // For some reason bedrock seems to send variant 0 for many entities that don't have variants
                            ViaBedrock.getPlatform().getLogger().warning("Received non-zero VARIANT " + variant + " for unsupported entity " + entity.type());
                        }
                    }
                }

            }
            case MARK_VARIANT -> {
                ViaBedrock.getPlatform().getLogger().warning("MARK_VARIANT " + entity.type() + " - " + entityData.getValue());
            }
            case OWNER -> {
                //TODO: Entity tracker
                /*EntityTracker entityTracker = null;
                long ownerId = (long) entityData.getValue(); //TODO: Check if its a Uid or Rid
                UUID uuid = entityTracker.getEntityByUid(ownerId).javaUuid();
                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.TAMABLE_ANIMAL)) {
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("OWNERUUID"), VersionedTypes.V1_21_9.entityDataTypes().optionalUUIDType, uuid));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received OWNER for non-TAMEABLE_ANIMAL entity " + entity.type());
                }*/
            }
            case FUSE_TIME -> {
                int fuseTime = (int) entityData.getValue();
                if (entity.javaType().is(EntityTypes1_21_9.TNT)) {
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("FUSE"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, fuseTime));
                } else {
                    //ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received FUSE_TIME for non-TNT entity " + entity.type());
                }
            }
            case AIR_SUPPLY -> { // Air supply is stored as a short in Bedrock, but an int in Java (Bedrock also has a max air supply value we ignore for now)
                short airSupply = (short) entityData.getValue();
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("AIR_SUPPLY"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, (int) airSupply));
            }
            case POSE_INDEX -> {
                break; // TODO: Armour stand pose index
            }
            case SCORE -> {
                /*int score = (int) entityData.getValue();
                if (entity.javaType().is(EntityTypes1_21_9.PLAYER)) {
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("SCORE"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, score));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received SCORE for non-PLAYER entity " + entity.type());
                }*/
            }
            case PUFFED_STATE -> {
                byte puffedState = (byte) entityData.getValue();
                int javaPuffedState = (int) puffedState;
                if (entity.javaType().is(EntityTypes1_21_9.PUFFERFISH)) {
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("PUFF_STATE"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, javaPuffedState));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received PUFFED_STATE for non-PUFFERFISH entity " + entity.type());
                }
            }
            case FREEZING_EFFECT_STRENGTH -> {
                float freezingStrength = (float) entityData.getValue();

                // Java freezing strength is from 0-140 whereas Bedrock is from 0.0-1.0
                int javaStrength = Math.round(freezingStrength * 140f);
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TICKS_FROZEN"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, javaStrength));
            }
            default -> {
                return false;
            }
        }

        return true;
    }
}
