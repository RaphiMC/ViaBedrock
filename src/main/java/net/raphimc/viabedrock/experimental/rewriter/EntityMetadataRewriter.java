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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorFlags;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class EntityMetadataRewriter {

    // Called in Entity#translateEntityData if experimental features are enabled
    public static boolean rewrite(final UserConnection user, final Entity entity, final ActorDataIDs id, final EntityData entityData, final List<EntityData> javaEntityData) {
        EntityTracker entityTracker = user.get(EntityTracker.class);

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

                if (entity.javaType().is(EntityTypes1_21_9.SNIFFER)) {
                    int sniffingState = 0;
                    if (bedrockFlags.contains(ActorFlags.IDLING)) {
                        sniffingState = 0;
                    } else if (false) {
                        //TODO: FEELING_HAPPY
                        sniffingState = 1;
                    } else if (false) {
                        //TODO: SCENTING
                        sniffingState = 2;
                    } else if (bedrockFlags.contains(ActorFlags.SNIFFING)) {
                        sniffingState = 3;
                    } else if (bedrockFlags.contains(ActorFlags.SEARCHING)) {
                        sniffingState = 4;
                    } else if (bedrockFlags.contains(ActorFlags.DIGGING)) {
                        sniffingState = 5;
                    } else if (false) {
                        //TODO: RISING
                        sniffingState = 6;
                    } else {
                        sniffingState = 0;
                        //TODO: Currently spams a bit but thats probably because we are missing states
                        //ViaBedrock.getPlatform().getLogger().warning("Unknown sniffer state, defaulting to IDLING.");
                    }

                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("STATE"), VersionedTypes.V1_21_9.entityDataTypes().snifferState, sniffingState));
                }

                if (entity.javaType().is(EntityTypes1_21_9.TURTLE)) { //TODO: Test
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("HAS_EGG"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, bedrockFlags.contains(ActorFlags.IS_PREGNANT)));
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("LAYING_EGG"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, bedrockFlags.contains(ActorFlags.LAYING_EGG)));
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

                if (entity.javaType().is(EntityTypes1_21_9.CAT)) { //TODO: Test
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("IS_LYING"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, bedrockFlags.contains(ActorFlags.LAYING_DOWN)));
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

                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.ABSTRACT_RAIDER)) { //TODO: Test
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
                                ViaBedrock.getPlatform().getLogger().warning("Unknown wolf variant " + variant + ", defaulting to PALE.");
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
                                ViaBedrock.getPlatform().getLogger().warning("Unknown frog variant " + variant + ", defaulting to TEMPERATE.");
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
                    case SHULKER -> {
                        byte color = (byte) variant;
                        javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("COLOR"), VersionedTypes.V1_21_9.entityDataTypes().byteType, color));
                    }
                    case AXOLOTL -> {
                        int javaVariant = switch (variant) {
                            case 0 -> 0; // LUCY
                            case 1 -> 3; // CYAN
                            case 2 -> 2; // GOLD
                            case 3 -> 1; // WILD
                            case 4 -> 4; // BLUE
                            default -> {
                                ViaBedrock.getPlatform().getLogger().warning("Unknown axolotl variant " + variant + ", defaulting to LUCY.");
                                yield 2;
                            }
                        };
                        javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("VARIANT"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, javaVariant));
                    }
                    default -> {
                        if (variant != 0) { // For some reason bedrock seems to send variant 0 for many entities that don't have variants
                            ViaBedrock.getPlatform().getLogger().warning("Received non-zero VARIANT " + variant + " for unsupported entity " + entity.type());
                        }
                    }
                }

            }
            case COLOR_INDEX -> {
                byte colorIndex = (byte) entityData.getValue();
                int javaColorIndex = colorIndex;

                switch (entity.javaType()) {
                    case WOLF, CAT -> {
                        javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("COLLAR_COLOR"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, javaColorIndex));
                    }
                    default -> {
                        if (colorIndex != 0) { // For some reason bedrock seems to send color index 0 for many entities that don't have colors
                            ViaBedrock.getPlatform().getLogger().warning("Received non-zero COLOR_INDEX " + colorIndex + " for unsupported entity " + entity.type());
                        }
                    }
                }
            }
            case OWNER -> {
                long ownerId = (long) entityData.getValue();
                if (ownerId == -1) {
                    break; // No owner
                }
                Entity ownerEntity = entityTracker.getEntityByUid(ownerId);
                if (ownerEntity == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find owner entity with id " + ownerId + " for entity " + entity.type());
                    break;
                }
                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.TAMABLE_ANIMAL)) {
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("OWNERUUID"), VersionedTypes.V1_21_9.entityDataTypes().optionalUUIDType, ownerEntity.javaUuid()));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received OWNER for non-TAMEABLE_ANIMAL entity " + entity.type());
                }
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
                if (!entity.javaType().is(EntityTypes1_21_9.ARMOR_STAND)) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received POSE_INDEX for non-ARMOR_STAND entity " + entity.type());
                    break;
                }

                byte javaBitMask = 0;
                javaBitMask |= 0x04; // Has arms

                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("CLIENT_FLAGS"), VersionedTypes.V1_21_9.entityDataTypes().byteType, javaBitMask));

                int poseIndex = (int) entityData.getValue();

                EulerAngle headPose;
                EulerAngle bodyPose;
                EulerAngle leftArmPose;
                EulerAngle rightArmPose;
                EulerAngle leftLegPose;
                EulerAngle rightLegPose;

                //Poses from https://github.com/lpsmods/armor-stand-poses/blob/1.21/datapack/datapack/data/poses/function/armor_stand/defaults.mcfunction
                switch (poseIndex) {
                    case 0 -> { // DEFAULT
                        headPose = new EulerAngle(0f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(-10f, 0f, -10f);
                        rightArmPose = new EulerAngle(-15f, 0f, 10f);
                        leftLegPose = new EulerAngle(-1f, 0f, -1f);
                        rightLegPose = new EulerAngle(1f, 0f, 1f);
                    }
                    case 1 -> { // NONE
                        headPose = new EulerAngle(0f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(0f, 0f, 0f);
                        rightArmPose = new EulerAngle(0f, 0f, 0f);
                        leftLegPose = new EulerAngle(0f, 0f, 0f);
                        rightLegPose = new EulerAngle(0f, 0f, 0f);
                    }
                    case 2 -> { // SOLEMN
                        headPose = new EulerAngle(15f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 2f);
                        leftArmPose = new EulerAngle(-30f, 15f, 15f);
                        rightArmPose = new EulerAngle(-60f, -20f, -10f);
                        leftLegPose = new EulerAngle(-1f, 0f, -1f);
                        rightLegPose = new EulerAngle(1f, 0f, 1f);
                    }
                    case 3 -> { // ATHENA
                        headPose = new EulerAngle(-5f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 2f);
                        leftArmPose = new EulerAngle(10f, 0f, -5f);
                        rightArmPose = new EulerAngle(-60f, 20f, -10f);
                        leftLegPose = new EulerAngle(-3f, -3f, -3f);
                        rightLegPose = new EulerAngle(3f, 3f, 3f);
                    }
                    case 4 -> { // BRANDISH
                        headPose = new EulerAngle(-15f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, -2f);
                        leftArmPose = new EulerAngle(20f, 0f, -10f);
                        rightArmPose = new EulerAngle(-110f, 50f, 0f);
                        leftLegPose = new EulerAngle(5f, -3f, -3f);
                        rightLegPose = new EulerAngle(-5f, 3f, 3f);
                    }
                    case 5 -> { // HONOR
                        headPose = new EulerAngle(-15f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(-110f, 35f, 0f);
                        rightArmPose = new EulerAngle(-110f, -35f, 0f);
                        leftLegPose = new EulerAngle(5f, -3f, -3f);
                        rightLegPose = new EulerAngle(-5f, 3f, 3f);
                    }
                    case 6 -> { // ENTERTAIN
                        headPose = new EulerAngle(-15f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(-110f, -35f, 0f);
                        rightArmPose = new EulerAngle(-110f, 35f, 0f);
                        leftLegPose = new EulerAngle(5f, -3f, -3f);
                        rightLegPose = new EulerAngle(-5f, 3f, 3f);
                    }
                    case 7 -> { // SALUTE
                        headPose = new EulerAngle(0f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(10f, 0f, -5f);
                        rightArmPose = new EulerAngle(-70f, -40f, 0f);
                        leftLegPose = new EulerAngle(-1f, 0f, -1f);
                        rightLegPose = new EulerAngle(1f, 0f, 1f);
                    }
                    case 8 -> { // RIPOSTE
                        headPose = new EulerAngle(16f, 20f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(4f, 8f, 237f);
                        rightArmPose = new EulerAngle(246f, 0f, 89f);
                        leftLegPose = new EulerAngle(-14f, -18f, -16f);
                        rightLegPose = new EulerAngle(8f, 20f, 4f);
                    }
                    case 9 -> { // ZOMBIE
                        headPose = new EulerAngle(-10f, 0f, -5f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(-105f, 0f, 0f);
                        rightArmPose = new EulerAngle(-100f, 0f, 0f);
                        leftLegPose = new EulerAngle(7f, 0f, 0f);
                        rightLegPose = new EulerAngle(-46f, 0f, 0f);
                    }
                    case 10 -> { // CAN_CAN_A
                        headPose = new EulerAngle(-5f, 18f, 0f);
                        bodyPose = new EulerAngle(0f, 22f, 0f);
                        leftArmPose = new EulerAngle(8f, 0f, -114f);
                        rightArmPose = new EulerAngle(0f, 84f, 111f);
                        leftLegPose = new EulerAngle(-111f, 55f, 0f);
                        rightLegPose = new EulerAngle(0f, 23f, -13f);
                    }
                    case 11 -> { // CAN_CAN_B
                        headPose = new EulerAngle(-10f, -20f, 0f);
                        bodyPose = new EulerAngle(0f, -18f, 0f);
                        leftArmPose = new EulerAngle(0f, 0f, -112f);
                        rightArmPose = new EulerAngle(8f, 90f, 111f);
                        leftLegPose = new EulerAngle(0f, 0f, 13f);
                        rightLegPose = new EulerAngle(-119f, -42f, 0f);
                    }
                    case 12 -> { // HERO
                        headPose = new EulerAngle(-4f, 67f, 0f);
                        bodyPose = new EulerAngle(0f, 8f, 0f);
                        leftArmPose = new EulerAngle(16f, 32f, -8f);
                        rightArmPose = new EulerAngle(-99f, 63f, 0f);
                        leftLegPose = new EulerAngle(0f, -75f, -8f);
                        rightLegPose = new EulerAngle(4f, 63f, 8f);
                    }
                    default -> {
                        // Fallback to none
                        headPose = new EulerAngle(0f, 0f, 0f);
                        bodyPose = new EulerAngle(0f, 0f, 0f);
                        leftArmPose = new EulerAngle(0f, 0f, 0f);
                        rightArmPose = new EulerAngle(0f, 0f, 0f);
                        leftLegPose = new EulerAngle(0f, 0f, 0f);
                        rightLegPose = new EulerAngle(0f, 0f, 0f);
                        ViaBedrock.getPlatform().getLogger().warning("Unknown armor stand pose index " + poseIndex + ", defaulting to NONE.");
                    }
                }

                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("HEAD_POSE"), VersionedTypes.V1_21_9.entityDataTypes().rotationsType, headPose));
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("BODY_POSE"), VersionedTypes.V1_21_9.entityDataTypes().rotationsType, bodyPose));
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("LEFT_ARM_POSE"), VersionedTypes.V1_21_9.entityDataTypes().rotationsType, leftArmPose));
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("RIGHT_ARM_POSE"), VersionedTypes.V1_21_9.entityDataTypes().rotationsType, rightArmPose));
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("LEFT_LEG_POSE"), VersionedTypes.V1_21_9.entityDataTypes().rotationsType, leftLegPose));
                javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("RIGHT_LEG_POSE"), VersionedTypes.V1_21_9.entityDataTypes().rotationsType, rightLegPose));
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
            case GOAT_HORN_COUNT -> {
                if (entity.javaType().is(EntityTypes1_21_9.GOAT)) {
                    // In bedrock the goat always loses its right horn first, whereas in java its random
                    int hornCount = (int) entityData.getValue();
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("HAS_LEFT_HORN"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, hornCount != 0));
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("HAS_RIGHT_HORN"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, hornCount == 2));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received GOAT_HORN_COUNT for non-GOAT entity " + entity.type());
                }
            }
            case EATING_COUNTER -> {
                int eatingCounter = (int) entityData.getValue();
                if (entity.javaType().is(EntityTypes1_21_9.PANDA)) {
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("EAT_COUNTER"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, eatingCounter));
                } else if (eatingCounter != 0) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received EATING_COUNTER for non-PANDA entity " + entity.type() + " with non-zero value " + eatingCounter);
                }
            }
            case ATTACH_FACE -> {
                if (entity.javaType().is(EntityTypes1_21_9.SHULKER)) {
                    byte attachFace = (byte) entityData.getValue();
                    int javaAttachFace = attachFace;
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("ATTACH_FACE"), VersionedTypes.V1_21_9.entityDataTypes().directionType, javaAttachFace));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received ATTACH_FACE for non-SHULKER entity " + entity.type());
                }
            }
            case PEEK_ID -> {
                if (entity.javaType().is(EntityTypes1_21_9.SHULKER)) {
                    int peekId = (int) entityData.getValue();
                    byte peek = (byte) peekId;
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("PEEK"), VersionedTypes.V1_21_9.entityDataTypes().byteType, peek));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received PEEK_ID for non-SHULKER entity " + entity.type());
                }
            }
            case ATTACHED, ATTACH_POS -> { // Not needed in java
                if (!entity.javaType().is(EntityTypes1_21_9.SHULKER)) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received ATTACH for non-SHULKER entity " + entity.type());
                }
            }
            case DATA_RADIUS -> {
                if (entity.javaType().isOrHasParent(EntityTypes1_21_9.AREA_EFFECT_CLOUD)) {
                    float radius = (float) entityData.getValue();
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("RADIUS"), VersionedTypes.V1_21_9.entityDataTypes().floatType, radius));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received DATA_RADIUS for non-AREA_EFFECT_CLOUD entity " + entity.type());
                }
            }
            case DATA_WAITING -> {
                if (entity.javaType().is(EntityTypes1_21_9.AREA_EFFECT_CLOUD)) {
                    boolean isWaiting = (boolean) entityData.getValue();
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("WAITING"), VersionedTypes.V1_21_9.entityDataTypes().booleanType, isWaiting));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received DATA_WAITING for non-AREA_EFFECT_CLOUD entity " + entity.type());
                }
            }
            case DATA_PARTICLE -> {
                if (entity.javaType().is(EntityTypes1_21_9.AREA_EFFECT_CLOUD)) {
                    int particle_id_or_colour = (int) entityData.getValue(); //TODO: not sure what this is exactly
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received DATA_PARTICLE for non-AREA_EFFECT_CLOUD entity " + entity.type());
                }
            }
            case INV -> {
                if (entity.javaType().is(EntityTypes1_21_9.WITHER)) {
                    int invulnerabilityTicks = (int) entityData.getValue();
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("INV"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, invulnerabilityTicks));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received INV for non-WITHER entity " + entity.type());
                }
            }
            case TARGET_A -> {
                if (entity.javaType().is(EntityTypes1_21_9.WITHER)) {
                    long targetAId = (long) entityData.getValue();
                    if (targetAId == -1) {
                        break; // No target
                    }
                    Entity targetAEntity = entityTracker.getEntityByUid(targetAId);
                    if (targetAEntity == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find TARGET_A entity with id " + targetAId + " for entity " + entity.type());
                        break;
                    }
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TARGET_A"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, targetAEntity.javaId()));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received TARGET_A for non-WITHER entity " + entity.type());
                }
            }
            case TARGET_B -> {
                if (entity.javaType().is(EntityTypes1_21_9.WITHER)) {
                    long targetBId = (long) entityData.getValue();
                    if (targetBId == -1) {
                        break; // No target
                    }
                    Entity targetBEntity = entityTracker.getEntityByUid(targetBId);
                    if (targetBEntity == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find TARGET_B entity with id " + targetBId + " for entity " + entity.type());
                        break;
                    }
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TARGET_B"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, targetBEntity.javaId()));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received TARGET_B for non-WITHER entity " + entity.type());
                }
            }
            case TARGET_C -> {
                if (entity.javaType().is(EntityTypes1_21_9.WITHER)) {
                    long targetCId = (long) entityData.getValue();
                    if (targetCId == -1) {
                        break; // No target
                    }
                    Entity targetCEntity = entityTracker.getEntityByUid(targetCId);
                    if (targetCEntity == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find TARGET_C entity with id " + targetCId + " for entity " + entity.type());
                        break;
                    }
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("TARGET_C"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, targetCEntity.javaId()));
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received TARGET_C for non-WITHER entity " + entity.type());
                }
            }
            case TARGET -> {
                long targetId = (long) entityData.getValue();
                if (entity.javaType().is(EntityTypes1_21_9.GUARDIAN)) {
                    if (targetId == 0) {
                        break; // No target
                    }
                    Entity targetEntity = entityTracker.getEntityByUid(targetId);
                    if (targetEntity == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Could not find TARGET entity with id " + targetId + " for entity " + entity.type());
                        break;
                    }
                    javaEntityData.add(new EntityData(entity.getJavaEntityDataIndex("ATTACK_TARGET"), VersionedTypes.V1_21_9.entityDataTypes().varIntType, targetEntity.javaId()));
                } else if (targetId != 0)  {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received TARGET for non-GUARDIAN entity " + entity.type() + " with non-zero value " + targetId);
                }
            }
            case AGENT, BALLOON_ANCHOR -> {} // Education edition only, ignore
            default -> {
                return false;
            }
        }

        return true;
    }
}
