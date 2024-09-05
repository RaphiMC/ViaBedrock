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
package net.raphimc.viabedrock.protocol.packet;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.model.resourcepack.SoundDefinitions;
import net.raphimc.viabedrock.api.util.EnumUtil;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.data.enums.Dimension;
import net.raphimc.viabedrock.protocol.data.enums.Direction;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.LevelEvent;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.NoteBlockInstrument;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ParticleType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.Puv_Legacy_LevelSoundEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.data.enums.java.PositionSourceType;
import net.raphimc.viabedrock.protocol.data.enums.java.SoundSource;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class WorldEffectPackets {

    // Only log warnings about missing mappings if explicitly enabled for debugging
    // The Bedrock Dedicated Server sends a lot of unknown sound events which are expected to be ignored in most cases (Resource packs could add custom sounds for certain events)
    private static final boolean LEVEL_SOUND_DEBUG_LOG = false;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.PLAY_SOUND, ClientboundPackets1_21.SOUND, wrapper -> {
            final String name = wrapper.read(BedrockTypes.STRING); // sound name
            final BlockPosition position = wrapper.read(BedrockTypes.BLOCK_POSITION); // position
            final float volume = wrapper.read(BedrockTypes.FLOAT_LE); // volume
            final float pitch = wrapper.read(BedrockTypes.FLOAT_LE); // pitch

            final BedrockMappingData.JavaSound javaSound = BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().get(name);
            if (javaSound == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock sound: " + name);
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.SOUND_EVENT, Holder.of(javaSound.id())); // sound id
            wrapper.write(Types.VAR_INT, javaSound.category().ordinal()); // category
            wrapper.write(Types.INT, position.x()); // x
            wrapper.write(Types.INT, position.y()); // y
            wrapper.write(Types.INT, position.z()); // z
            wrapper.write(Types.FLOAT, volume); // volume
            wrapper.write(Types.FLOAT, pitch); // pitch
            wrapper.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // seed
        });
        protocol.registerClientbound(ClientboundBedrockPackets.STOP_SOUND, ClientboundPackets1_21.STOP_SOUND, wrapper -> {
            final String name = wrapper.read(BedrockTypes.STRING); // sound name
            final boolean stopAll = wrapper.read(Types.BOOLEAN); // stop all
            wrapper.read(Types.BOOLEAN); // stop music | Ignored because it seems to do nothing

            if (stopAll) {
                wrapper.write(Types.BYTE, (byte) 0); // flags
            } else {
                final BedrockMappingData.JavaSound javaSound = BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().get(name);
                if (javaSound == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock sound: " + name);
                    wrapper.cancel();
                    return;
                }

                wrapper.write(Types.BYTE, (byte) 2); // flags
                wrapper.write(Types.STRING, javaSound.identifier()); // sound identifier
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.SPAWN_PARTICLE_EFFECT, ClientboundPackets1_21.LEVEL_PARTICLES, wrapper -> {
            final Dimension dimension = Dimension.getByValue(wrapper.read(Types.BYTE)); // dimension
            if (dimension != wrapper.user().get(ChunkTracker.class).getDimension()) {
                wrapper.cancel();
                return;
            }
            wrapper.read(BedrockTypes.VAR_LONG); // unique entity id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final String effectIdentifier = wrapper.read(BedrockTypes.STRING); // effect name
            if (wrapper.read(Types.BOOLEAN)) { // has molang variables
                wrapper.read(BedrockTypes.STRING); // molang variables json
            }

            final BedrockMappingData.JavaParticle javaParticle = BedrockProtocol.MAPPINGS.getBedrockToJavaParticles().get(effectIdentifier);
            if (javaParticle == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock particle: " + effectIdentifier);
                wrapper.cancel();
                return;
            }
            PacketFactory.writeJavaLevelParticles(wrapper, position, javaParticle);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_SOUND_EVENT_V1, ClientboundPackets1_21.SOUND, wrapper -> {
            final byte rawSoundEvent = wrapper.read(Types.BYTE); // event id
            final Puv_Legacy_LevelSoundEvent soundEvent = Puv_Legacy_LevelSoundEvent.getByValue(rawSoundEvent);
            if (soundEvent == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown Puv_Legacy_LevelSoundEvent: " + rawSoundEvent);
                wrapper.cancel();
                return;
            }
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final int data = wrapper.read(BedrockTypes.VAR_INT); // data
            final int entityType = wrapper.read(BedrockTypes.VAR_INT); // entity type
            final boolean isBabyMob = wrapper.read(Types.BOOLEAN); // is baby mob
            final boolean isGlobal = wrapper.read(Types.BOOLEAN); // is global sound

            final String entityIdentifier = BedrockProtocol.MAPPINGS.getBedrockEntities().inverse().getOrDefault(entityType, "");
            if (entityIdentifier.isEmpty()) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock entity type: " + entityType);
            }
            handleLevelSoundEvent(wrapper, soundEvent, position, data, entityIdentifier, isBabyMob, isGlobal);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_SOUND_EVENT_V2, ClientboundPackets1_21.SOUND, wrapper -> {
            final byte rawSoundEvent = wrapper.read(Types.BYTE); // event id
            final Puv_Legacy_LevelSoundEvent soundEvent = Puv_Legacy_LevelSoundEvent.getByValue(rawSoundEvent);
            if (soundEvent == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown Puv_Legacy_LevelSoundEvent: " + rawSoundEvent);
                wrapper.cancel();
                return;
            }
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final int data = wrapper.read(BedrockTypes.VAR_INT); // data
            final String entityIdentifier = wrapper.read(BedrockTypes.STRING); // entity identifier
            final boolean isBabyMob = wrapper.read(Types.BOOLEAN); // is baby mob
            final boolean isGlobal = wrapper.read(Types.BOOLEAN); // is global sound

            handleLevelSoundEvent(wrapper, soundEvent, position, data, entityIdentifier, isBabyMob, isGlobal);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_SOUND_EVENT, ClientboundPackets1_21.SOUND, wrapper -> {
            final int rawSoundEvent = wrapper.read(BedrockTypes.UNSIGNED_VAR_INT); // event id
            final Puv_Legacy_LevelSoundEvent soundEvent = Puv_Legacy_LevelSoundEvent.getByValue(rawSoundEvent);
            if (soundEvent == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown Puv_Legacy_LevelSoundEvent: " + rawSoundEvent);
                wrapper.cancel();
                return;
            }
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            final int data = wrapper.read(BedrockTypes.VAR_INT); // data
            final String entityIdentifier = wrapper.read(BedrockTypes.STRING); // entity identifier
            final boolean isBabyMob = wrapper.read(Types.BOOLEAN); // is baby mob
            final boolean isGlobal = wrapper.read(Types.BOOLEAN); // is global sound

            handleLevelSoundEvent(wrapper, soundEvent, position, data, entityIdentifier, isBabyMob, isGlobal);
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_EVENT, ClientboundPackets1_21.LEVEL_EVENT, wrapper -> {
            final int rawLevelEvent = wrapper.read(BedrockTypes.VAR_INT); // event id
            final Position3f position = wrapper.read(BedrockTypes.POSITION_3F); // position
            int data = wrapper.read(BedrockTypes.VAR_INT); // data

            if ((rawLevelEvent & LevelEvent.ParticleLegacyEvent.getValue()) != 0 || rawLevelEvent == LevelEvent.ParticleGenericSpawn.getValue()) {
                wrapper.setPacketType(ClientboundPackets1_21.LEVEL_PARTICLES);
                final int rawParticleType = rawLevelEvent == LevelEvent.ParticleGenericSpawn.getValue() ? data : rawLevelEvent & ~LevelEvent.ParticleLegacyEvent.getValue();
                final ParticleType particleType = ParticleType.getByValue(rawParticleType);
                if (particleType == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown particle type: " + rawParticleType);
                    wrapper.cancel();
                    return;
                }
                if (rawLevelEvent == LevelEvent.ParticleGenericSpawn.getValue()) {
                    data = 0;
                }
                final BedrockMappingData.JavaParticle javaParticle = BedrockProtocol.MAPPINGS.getBedrockToJavaLevelEventParticles().get(particleType);
                if (javaParticle != null) {
                    PacketFactory.writeJavaLevelParticles(wrapper, position, switch (particleType) {
                        case IconCrack, Food -> {
                            final BedrockItem bedrockItem = new BedrockItem(data >> 16, (short) (data & 0xFFFF), (byte) 1);
                            final Particle particle = new Particle(javaParticle.particle().id());
                            particle.add(Types1_21.ITEM, wrapper.user().get(ItemRewriter.class).javaItem(bedrockItem)); // item
                            yield javaParticle.withParticle(particle);
                        }
                        case Terrain, BrushDust -> {
                            final int javaBlockState = wrapper.user().get(BlockStateRewriter.class).javaId(data);
                            if (javaBlockState != -1) {
                                final Particle particle = new Particle(javaParticle.particle().id());
                                particle.add(Types.VAR_INT, javaBlockState); // block state
                                yield javaParticle.withParticle(particle);
                            } else {
                                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + data);
                                wrapper.cancel();
                                yield javaParticle;
                            }
                        }
                        case FallingDust -> {
                            final Particle particle = new Particle(javaParticle.particle().id());
                            final int r = (data >> 16) & 0xff;
                            final int g = (data >> 8) & 0xff;
                            final int b = data & 0xff;
                            particle.add(Types.FLOAT, r / 255F); // from red
                            particle.add(Types.FLOAT, g / 255F); // from green
                            particle.add(Types.FLOAT, b / 255F); // from blue
                            particle.add(Types.FLOAT, r / 255F); // to red
                            particle.add(Types.FLOAT, g / 255F); // to green
                            particle.add(Types.FLOAT, b / 255F); // to blue
                            particle.add(Types.FLOAT, 1F); // scale
                            yield javaParticle.withParticle(particle);
                        }
                        case MobSpell -> {
                            final Particle particle = new Particle(javaParticle.particle().id());
                            particle.add(Types.INT, (0xFF << 24) | data); // color
                            yield javaParticle.withParticle(particle);
                        }
                        default -> javaParticle;
                    });
                } else {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing level event particle mapping for " + particleType);
                    wrapper.cancel();
                }
                return;
            }

            final LevelEvent levelEvent = LevelEvent.getByValue(rawLevelEvent);
            if (levelEvent == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown LevelEvent: " + rawLevelEvent);
                wrapper.cancel();
                return;
            }
            switch (levelEvent) {
                case ParticleSoundGuardianGhost -> {
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.GUARDIAN_ELDER_EFFECT, 1F);
                    wrapper.cancel();
                }
                case StartRaining -> {
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.START_RAINING, 0F);
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.RAIN_LEVEL_CHANGE, data / 65535F);
                    wrapper.cancel();
                }
                case StopRaining -> {
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.STOP_RAINING, 0F);
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.RAIN_LEVEL_CHANGE, 0F);
                    wrapper.cancel();
                }
                case StartThunderstorm -> {
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.START_RAINING, 0F);
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.THUNDER_LEVEL_CHANGE, data / 65535F);
                    wrapper.cancel();
                }
                case StopThunderstorm -> {
                    PacketFactory.sendJavaGameEvent(wrapper.user(), GameEventType.THUNDER_LEVEL_CHANGE, 0F);
                    wrapper.cancel();
                }
                case GlobalPause -> {
                    if (data != 0) {
                        ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Server paused the game. This is not supported by ViaBedrock.");
                    }
                    wrapper.cancel();
                }
                case SimTimeStep -> {
                    ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Server tick stepped the game. This is not supported by ViaBedrock.");
                    wrapper.cancel();
                }
                case SimTimeScale -> {
                    ViaBedrock.getPlatform().getLogger().log(Level.SEVERE, "Server sped up the game. This is not supported by ViaBedrock.");
                    wrapper.cancel();
                }
                case StartBlockCracking, StopBlockCracking, UpdateBlockCracking -> {
                    wrapper.cancel(); // TODO: Implement block break progress translation
                }
                default -> {
                    BedrockMappingData.LevelEventMapping levelEventMapping = BedrockProtocol.MAPPINGS.getBedrockToJavaLevelEvents().get(levelEvent);
                    if (levelEventMapping instanceof BedrockMappingData.JavaSoundLevelEvent javaSoundLevelEvent) {
                        if (data == 0) {
                            levelEventMapping = javaSoundLevelEvent.levelEvent();
                        } else {
                            levelEventMapping = javaSoundLevelEvent.sound();
                        }
                    }
                    if (levelEventMapping instanceof BedrockMappingData.JavaLevelEvent javaLevelEvent) {
                        wrapper.write(Types.INT, javaLevelEvent.levelEvent().getValue()); // event id
                        wrapper.write(Types.BLOCK_POSITION1_14, new BlockPosition((int) position.x(), (int) position.y(), (int) position.z())); // position
                        wrapper.write(Types.INT, switch (levelEvent) {
                            case ParticlesShoot, ParticlesShootWhiteSmoke -> switch (data % 9) {
                                case 3, 0 -> Direction.WEST.ordinal();
                                case 4 -> Direction.UP.ordinal();
                                case 5, 8 -> Direction.EAST.ordinal();
                                case 7, 6 -> Direction.SOUTH.ordinal();
                                default /* 1, 2 */ -> Direction.NORTH.ordinal();
                            };
                            case ParticlesDestroyBlock, ParticlesDestroyBlockNoSound -> {
                                final int javaBlockState = wrapper.user().get(BlockStateRewriter.class).javaId(data);
                                if (javaBlockState != -1) {
                                    yield javaBlockState;
                                } else {
                                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + data);
                                    wrapper.cancel();
                                    yield 0;
                                }
                            }
                            default -> javaLevelEvent.data() != null ? javaLevelEvent.data() : data;
                        }); // data
                        wrapper.write(Types.BOOLEAN, false); // global
                    } else if (levelEventMapping instanceof BedrockMappingData.JavaSound javaSound) {
                        if (data < 0 || data > 256_000) data = 1000;
                        wrapper.setPacketType(ClientboundPackets1_21.SOUND);
                        wrapper.write(Types.SOUND_EVENT, Holder.of(javaSound.id())); // sound id
                        wrapper.write(Types.VAR_INT, javaSound.category().ordinal()); // category
                        wrapper.write(Types.INT, (int) (position.x() * 8F)); // x
                        wrapper.write(Types.INT, (int) (position.y() * 8F)); // y
                        wrapper.write(Types.INT, (int) (position.z() * 8F)); // z
                        wrapper.write(Types.FLOAT, 1F); // volume
                        wrapper.write(Types.FLOAT, data / 1000F); // pitch
                        wrapper.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // seed
                    } else if (levelEventMapping instanceof BedrockMappingData.JavaParticle javaParticle) {
                        wrapper.setPacketType(ClientboundPackets1_21.LEVEL_PARTICLES);
                        PacketFactory.writeJavaLevelParticles(wrapper, switch (levelEvent) {
                            case ParticlesCrackBlockDown -> new Position3f((int) position.x() + 0.5F, (int) position.y(), (int) position.z() + 0.5F);
                            case ParticlesCrackBlockUp -> new Position3f((int) position.x() + 0.5F, (int) position.y() + 1F, (int) position.z() + 0.5F);
                            case ParticlesCrackBlockNorth -> new Position3f((int) position.x() + 0.5F, (int) position.y() + 0.5F, (int) position.z());
                            case ParticlesCrackBlockSouth -> new Position3f((int) position.x() + 0.5F, (int) position.y() + 0.5F, (int) position.z() + 1F);
                            case ParticlesCrackBlockWest -> new Position3f((int) position.x(), (int) position.y() + 0.5F, (int) position.z() + 0.5F);
                            case ParticlesCrackBlockEast -> new Position3f((int) position.x() + 1F, (int) position.y() + 0.5F, (int) position.z() + 0.5F);
                            default -> position;
                        }, switch (levelEvent) {
                            case ParticlesCrit -> javaParticle.withCount(data);
                            case ParticlesCrackBlock, ParticlesCrackBlockDown, ParticlesCrackBlockUp, ParticlesCrackBlockNorth,
                                 ParticlesCrackBlockSouth, ParticlesCrackBlockWest, ParticlesCrackBlockEast -> {
                                final int javaBlockState = wrapper.user().get(BlockStateRewriter.class).javaId(data);
                                if (javaBlockState != -1) {
                                    final Particle particle = new Particle(javaParticle.particle().id());
                                    particle.add(Types.VAR_INT, javaBlockState); // block state
                                    yield javaParticle.withParticle(particle);
                                } else {
                                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state: " + data);
                                    wrapper.cancel();
                                    yield javaParticle;
                                }
                            }
                            default -> javaParticle;
                        });
                    } else if (levelEventMapping == null) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing level event mapping for " + levelEvent);
                        wrapper.cancel();
                    } else {
                        throw new IllegalStateException("Unknown level event mapping type: " + levelEventMapping.getClass().getName());
                    }
                }
            }
        });
        protocol.registerClientbound(ClientboundBedrockPackets.LEVEL_EVENT_GENERIC, ClientboundPackets1_21.LEVEL_PARTICLES, wrapper -> {
            final int rawLevelEvent = wrapper.read(BedrockTypes.VAR_INT); // event id
            final CompoundTag data = (CompoundTag) wrapper.read(BedrockTypes.COMPOUND_TAG_VALUE); // data

            final LevelEvent levelEvent = LevelEvent.getByValue(rawLevelEvent);
            if (levelEvent == null) {
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown LevelEvent: " + rawLevelEvent);
                wrapper.cancel();
                return;
            }
            switch (levelEvent) {
                case ParticlesBlockExplosion -> {
                    wrapper.cancel();
                    final float originX = data.getFloat("originX");
                    final float originY = data.getFloat("originY");
                    final float originZ = data.getFloat("originZ");
                    final float radius = data.getFloat("radius");
                    final int blockCount = data.getInt("size");
                    for (int i = 0; i < blockCount; i++) {
                        final float x = data.getFloat("pos" + i + "x");
                        final float y = data.getFloat("pos" + i + "y");
                        final float z = data.getFloat("pos" + i + "z");
                        final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:smoke"));
                        final PacketWrapper levelParticles = PacketWrapper.create(ClientboundPackets1_21.LEVEL_PARTICLES, wrapper.user());
                        PacketFactory.writeJavaLevelParticles(levelParticles, new Position3f(x, y, z), new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0F, 0));
                        levelParticles.send(BedrockProtocol.class);
                    }
                }
                case ParticlesVibrationSignal -> {
                    if (data.get("origin") instanceof CompoundTag origin && data.get("target") instanceof CompoundTag target && data.get("timeToLive") instanceof FloatTag timeToLive) {
                        final Position3f originPosition = new Position3f(origin.getFloat("x"), origin.getFloat("y"), origin.getFloat("z"));
                        final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:vibration"));
                        switch (target.getString("type", "")) {
                            case "vec3" -> {
                                final Position3f targetPosition = new Position3f(target.getFloat("x"), target.getFloat("y"), target.getFloat("z"));
                                particle.add(Types.VAR_INT, PositionSourceType.BLOCK.ordinal()); // destination type
                                particle.add(Types.BLOCK_POSITION1_14, new BlockPosition((int) targetPosition.x(), (int) targetPosition.y(), (int) targetPosition.z())); // destination block pos
                            }
                            case "actor" -> {
                                final Entity entity = wrapper.user().get(EntityTracker.class).getEntityByUid(target.getLong("uniqueID"));
                                if (entity == null) {
                                    wrapper.cancel();
                                    return;
                                }
                                particle.add(Types.VAR_INT, PositionSourceType.ENTITY.ordinal()); // destination type
                                particle.add(Types.VAR_INT, entity.javaId()); // destination entity
                                particle.add(Types.FLOAT, 0F); // y offset
                            }
                            default -> {
                                wrapper.cancel();
                                return;
                            }
                        }
                        particle.add(Types.VAR_INT, (int) (timeToLive.asFloat() * 20F)); // arrival in ticks
                        PacketFactory.writeJavaLevelParticles(wrapper, originPosition, new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0F, 0));
                    } else {
                        wrapper.cancel();
                    }
                }
                case ParticlesSculkShriek -> {
                    wrapper.cancel();
                    final Position3f position = new Position3f(data.getInt("originX") + 0.5F, data.getInt("originY") + 0.5F, data.getInt("originZ") + 0.5F);
                    for (int i = 0; i < 15; i++) {
                        final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:shriek"));
                        particle.add(Types.VAR_INT, i * 5); // delay
                        final PacketWrapper levelParticles = PacketWrapper.create(ClientboundPackets1_21.LEVEL_PARTICLES, wrapper.user());
                        PacketFactory.writeJavaLevelParticles(levelParticles, position, new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0F, 0));
                        levelParticles.send(BedrockProtocol.class);
                    }
                }
                case SculkCatalystBloom -> {
                    final Position3f position = new Position3f(data.getFloat("originX"), data.getFloat("originY"), data.getFloat("originZ"));
                    final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:sculk_soul"));
                    PacketFactory.writeJavaLevelParticles(wrapper, new Position3f(position.x() + 0.5F, position.y() + 1.15F, position.z() + 0.5F), new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0F, 0));
                    final PacketWrapper sound = PacketWrapper.create(ClientboundPackets1_21.SOUND, wrapper.user());
                    sound.write(Types.SOUND_EVENT, Holder.of((int) BedrockProtocol.MAPPINGS.getJavaSounds().get("minecraft:block.sculk_catalyst.bloom"))); // sound id
                    sound.write(Types.VAR_INT, SoundSource.BLOCKS.ordinal()); // category
                    sound.write(Types.INT, (int) (position.x() * 8F)); // x
                    sound.write(Types.INT, (int) (position.y() * 8F)); // y
                    sound.write(Types.INT, (int) (position.z() * 8F)); // z
                    sound.write(Types.FLOAT, 2F); // volume
                    sound.write(Types.FLOAT, 0.6F + ThreadLocalRandom.current().nextFloat() * 0.4F); // pitch
                    sound.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // seed
                    sound.send(BedrockProtocol.class);
                }
                case SculkCharge -> {
                    wrapper.setPacketType(ClientboundPackets1_21.LEVEL_EVENT);
                    wrapper.write(Types.INT, net.raphimc.viabedrock.protocol.data.enums.java.LevelEvent.PARTICLES_SCULK_CHARGE.getValue()); // event id
                    wrapper.write(Types.BLOCK_POSITION1_14, new BlockPosition(data.getInt("x"), data.getInt("y"), data.getInt("z"))); // position
                    wrapper.write(Types.INT, (data.getShort("charge") << 6) | (data.getShort("facing") & 0x3F)); // data
                    wrapper.write(Types.BOOLEAN, false); // global
                }
                case SculkChargePop -> {
                    final Position3f position = new Position3f(data.getInt("x") + 0.5F, data.getInt("y") + 0.5F, data.getInt("z") + 0.5F);
                    final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:sculk_charge_pop"));
                    PacketFactory.writeJavaLevelParticles(wrapper, position, new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0.04F, 20));
                }
                case SonicExplosion -> {
                    final Position3f position = new Position3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                    final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:sonic_boom"));
                    PacketFactory.writeJavaLevelParticles(wrapper, position, new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0F, 0));
                }
                case DustPlume -> {
                    final Position3f position = new Position3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                    final Particle particle = new Particle(BedrockProtocol.MAPPINGS.getJavaParticles().get("minecraft:dust_plume"));
                    PacketFactory.writeJavaLevelParticles(wrapper, position, new BedrockMappingData.JavaParticle(particle, 0F, 0F, 0F, 0F, 7));
                }
                case SleepingPlayers -> {
                    // This shows the amount of players currently sleeping when in a bed
                    wrapper.cancel(); // TODO: Implement translation
                }
                default -> {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unhandled generic level event: " + levelEvent + " (" + data + ")");
                    wrapper.cancel();
                }
            }
        });
    }

    private static void handleLevelSoundEvent(final PacketWrapper wrapper, final Puv_Legacy_LevelSoundEvent soundEvent, final Position3f position, final int data, final String entityIdentifier, final boolean isBabyMob, final boolean isGlobal) {
        final boolean globalSound = isGlobal || Float.isNaN(position.x()) || Float.isNaN(position.y()) || Float.isNaN(position.z());

        SoundDefinitions.ConfiguredSound configuredSound;
        switch (soundEvent) {
            case RecordNull -> {
                wrapper.setPacketType(ClientboundPackets1_21.STOP_SOUND);
                wrapper.write(Types.BYTE, (byte) 1); // flags
                wrapper.write(Types.VAR_INT, SoundSource.RECORDS.ordinal()); // category id
                return;
            }
            case Note -> {
                final NoteBlockInstrument noteBlockInstrument = NoteBlockInstrument.getByValue(data >> 8);
                final int key = data & 0xFF;
                final float pitch = (float) Math.pow(2D, (double) (key - 12) / 12);
                configuredSound = new SoundDefinitions.ConfiguredSound(noteBlockInstrument.soundName(), 1F, 1F, pitch, pitch);
            }
            default -> {
                configuredSound = tryFindSound(wrapper.user(), soundEvent, data, entityIdentifier, isBabyMob);
                if (configuredSound == null) { // Fallback for some special handled sounds
                    switch (soundEvent) {
                        case AmbientBaby, MobWarningBaby, HurtBaby, DeathBaby, StepBaby, SpawnBaby -> {
                            final Puv_Legacy_LevelSoundEvent soundEventAdult = EnumUtil.getEnumConstantOrNull(Puv_Legacy_LevelSoundEvent.class, soundEvent.name().replace("Baby", ""));
                            configuredSound = tryFindSound(wrapper.user(), soundEventAdult, data, entityIdentifier, true);
                        }
                        case AmbientInWater, AmbientInAir -> configuredSound = tryFindSound(wrapper.user(), Puv_Legacy_LevelSoundEvent.Ambient, data, entityIdentifier, isBabyMob);
                    }
                }
                if (configuredSound == null) {
                    if (LEVEL_SOUND_DEBUG_LOG) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing level sound event mapping for " + soundEvent + " with entity identifier '" + entityIdentifier + "' and data " + data);
                    }
                    wrapper.cancel();
                    return;
                }
            }
        }

        final BedrockMappingData.JavaSound javaSound = BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().get(configuredSound.sound());
        wrapper.write(Types.SOUND_EVENT, Holder.of(javaSound.id())); // sound id
        wrapper.write(Types.VAR_INT, javaSound.category().ordinal()); // category
        wrapper.write(Types.INT, (int) (position.x() * 8F)); // x
        wrapper.write(Types.INT, (int) (position.y() * 8F)); // y
        wrapper.write(Types.INT, (int) (position.z() * 8F)); // z
        wrapper.write(Types.FLOAT, globalSound ? Integer.MAX_VALUE : MathUtil.randomFloatInclusive(configuredSound.minVolume(), configuredSound.maxVolume())); // volume
        wrapper.write(Types.FLOAT, MathUtil.randomFloatInclusive(configuredSound.minPitch(), configuredSound.maxPitch())); // pitch
        wrapper.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // seed
    }

    private static SoundDefinitions.ConfiguredSound tryFindSound(final UserConnection user, final Puv_Legacy_LevelSoundEvent soundEvent, final int data, final String entityIdentifier, final boolean isBabyMob) {
        if (soundEvent == null) {
            return null;
        }

        final Map<String, SoundDefinitions.ConfiguredSound> soundEvents = BedrockProtocol.MAPPINGS.getBedrockLevelSoundEvents().get(soundEvent);
        if (soundEvents == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unmapped bedrock level sound event: " + soundEvent);
            return null;
        }
        SoundDefinitions.ConfiguredSound configuredSound = null;
        if (!entityIdentifier.isEmpty()) { // entity specific sound
            configuredSound = soundEvents.get(Key.namespaced(entityIdentifier));
            if (isBabyMob && configuredSound != null) {
                configuredSound = new SoundDefinitions.ConfiguredSound(configuredSound.sound(), configuredSound.minVolume(), configuredSound.maxVolume(), configuredSound.minPitch() + 0.5F, configuredSound.maxPitch() + 0.5F);
            }
        }
        if (configuredSound == null && data != -1) { // block specific sound
            final BlockState blockState = user.get(BlockStateRewriter.class).blockState(data);
            if (blockState != null) {
                final String blockSound = BedrockProtocol.MAPPINGS.getBedrockBlockSounds().get(blockState.namespacedIdentifier());
                if (blockSound != null) {
                    configuredSound = soundEvents.get(blockSound);
                } else {
                    if (LEVEL_SOUND_DEBUG_LOG) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing " + soundEvent.name() + " sound for " + blockState.namespacedIdentifier());
                    }
                    configuredSound = soundEvents.get("stone");
                }
            } else {
                if (LEVEL_SOUND_DEBUG_LOG) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state (level sound event): " + data);
                }
                configuredSound = soundEvents.get("stone");
            }
        }
        if (configuredSound == null) {
            configuredSound = soundEvents.get(null); // generic sound
        }
        return configuredSound;
    }

}