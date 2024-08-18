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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.model.resourcepack.SoundDefinitions;
import net.raphimc.viabedrock.api.util.EnumUtil;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.BedrockMappingData;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.NoteBlockInstrument;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.Puv_Legacy_LevelSoundEvent;
import net.raphimc.viabedrock.protocol.data.enums.java.SoundSource;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class SoundPackets {

    // Only log warnings about missing mappings if explicitly enabled for debugging
    // The Bedrock Dedicated Server sends a lot of unknown sound events which are expected to be ignored in most cases (Resource packs could add custom sounds for certain events)
    private static final boolean DEBUG_LOG = false;

    public static void register(final BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.PLAY_SOUND, ClientboundPackets1_21.SOUND, wrapper -> {
            final String name = wrapper.read(BedrockTypes.STRING); // sound name
            final BlockPosition position = wrapper.read(BedrockTypes.BLOCK_POSITION); // position
            final float volume = wrapper.read(BedrockTypes.FLOAT_LE); // volume
            final float pitch = wrapper.read(BedrockTypes.FLOAT_LE); // pitch

            final BedrockMappingData.JavaSoundMapping javaSound = BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().get(name);
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
                final BedrockMappingData.JavaSoundMapping javaSound = BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().get(name);
                if (javaSound == null) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown bedrock sound: " + name);
                    wrapper.cancel();
                    return;
                }

                wrapper.write(Types.BYTE, (byte) 2); // flags
                wrapper.write(Types.STRING, javaSound.identifier()); // sound identifier
            }
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
                configuredSound = tryFindConfiguredSound(wrapper.user(), soundEvent, data, entityIdentifier, isBabyMob);
                if (configuredSound == null) { // Fallback for some special handled sounds
                    switch (soundEvent) {
                        case AmbientBaby, MobWarningBaby, HurtBaby, DeathBaby, StepBaby, SpawnBaby -> {
                            final Puv_Legacy_LevelSoundEvent soundEventAdult = EnumUtil.getEnumConstantOrNull(Puv_Legacy_LevelSoundEvent.class, soundEvent.name().replace("Baby", ""));
                            configuredSound = tryFindConfiguredSound(wrapper.user(), soundEventAdult, data, entityIdentifier, true);
                        }
                        case AmbientInWater, AmbientInAir -> {
                            configuredSound = tryFindConfiguredSound(wrapper.user(), Puv_Legacy_LevelSoundEvent.Ambient, data, entityIdentifier, isBabyMob);
                        }
                    }
                }
                if (configuredSound == null) {
                    if (DEBUG_LOG) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing level sound event mapping for " + soundEvent + " with entity identifier '" + entityIdentifier + "' and data " + data);
                    }
                    wrapper.cancel();
                    return;
                }
            }
        }

        final BedrockMappingData.JavaSoundMapping javaSound = BedrockProtocol.MAPPINGS.getBedrockToJavaSounds().get(configuredSound.sound());
        wrapper.write(Types.SOUND_EVENT, Holder.of(javaSound.id())); // sound id
        wrapper.write(Types.VAR_INT, javaSound.category().ordinal()); // category
        wrapper.write(Types.INT, (int) (position.x() * 8F)); // x
        wrapper.write(Types.INT, (int) (position.y() * 8F)); // y
        wrapper.write(Types.INT, (int) (position.z() * 8F)); // z
        wrapper.write(Types.FLOAT, globalSound ? Integer.MAX_VALUE : MathUtil.randomFloatInclusive(configuredSound.minVolume(), configuredSound.maxVolume())); // volume
        wrapper.write(Types.FLOAT, MathUtil.randomFloatInclusive(configuredSound.minPitch(), configuredSound.maxPitch())); // pitch
        wrapper.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // seed
    }

    private static SoundDefinitions.ConfiguredSound tryFindConfiguredSound(final UserConnection user, final Puv_Legacy_LevelSoundEvent soundEvent, final int data, final String entityIdentifier, final boolean isBabyMob) {
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
            if (blockState == null) {
                if (DEBUG_LOG) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing block state (level sound event): " + data);
                }
                configuredSound = soundEvents.get("stone");
            } else {
                final String blockSound = BedrockProtocol.MAPPINGS.getBedrockBlockSounds().get(blockState.namespacedIdentifier());
                if (blockSound == null) {
                    if (DEBUG_LOG) {
                        ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing " + soundEvent.name() + " sound for " + blockState.namespacedIdentifier());
                    }
                    configuredSound = soundEvents.get("stone");
                } else {
                    configuredSound = soundEvents.get(blockSound);
                }
            }
        }
        if (configuredSound == null) {
            configuredSound = soundEvents.get(null); // generic sound
        }
        return configuredSound;
    }

}
