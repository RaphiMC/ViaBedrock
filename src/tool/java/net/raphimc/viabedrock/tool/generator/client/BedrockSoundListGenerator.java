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
package net.raphimc.viabedrock.tool.generator.client;

import com.viaversion.viaversion.libs.gson.Gson;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.resourcepack.definition.BlockDefinitions;
import net.raphimc.viabedrock.api.resourcepack.definition.SoundDefinitions;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.SharedTypes_Legacy_LevelSoundEvent;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;
import net.raphimc.viabedrock.util.Util;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class BedrockSoundListGenerator {

    private static final Set<String> CANCELLED_SOUNDS = Set.of(
            "imitate.panda",
            "guardian.flop",
            "block.sculk_sensor.clicking",
            "block.sculk_sensor.clicking_stop",
            "block.sculk.place",
            "item.bone_meal.use",
            "mob.armor_stand.break",
            "mob.armor_stand.land",
            "mob.armor_stand.hit",
            "sniff",
            "emerge",
            "spit",
            "dig",
            "scream",
            "sound",
            "jealous",
            "idle",
            "slightly_angry",
            "note.bass"
    );

    private static final Set<SharedTypes_Legacy_LevelSoundEvent> SPECIAL_SOUNDS = Set.of(
            // Mapped in code
            SharedTypes_Legacy_LevelSoundEvent.RecordNull,
            SharedTypes_Legacy_LevelSoundEvent.Note,
            // Those seem to do nothing
            //SharedTypes_Legacy_LevelSoundEvent.RecordPlaying,
            SharedTypes_Legacy_LevelSoundEvent.ItemFizz,
            SharedTypes_Legacy_LevelSoundEvent.MobWarningBaby,
            SharedTypes_Legacy_LevelSoundEvent.HaggleIdle,
            SharedTypes_Legacy_LevelSoundEvent.Default,
            //SharedTypes_Legacy_LevelSoundEvent.SpawnBaby,
            SharedTypes_Legacy_LevelSoundEvent.Scared,
            SharedTypes_Legacy_LevelSoundEvent.JumpPrevent,
            //SharedTypes_Legacy_LevelSoundEvent.Bump,
            SharedTypes_Legacy_LevelSoundEvent.Undefined
            //SharedTypes_Legacy_LevelSoundEvent.LTReactionGlowStick,
            //SharedTypes_Legacy_LevelSoundEvent.LTReactionGlowStick2,
            //SharedTypes_Legacy_LevelSoundEvent.LTReactionLuminol,
            //SharedTypes_Legacy_LevelSoundEvent.LTReactionSalt
    );

    /* Last output:
        Unknown block sound: normal
        Unknown sound: jump.ladder
        Unknown sound: jump.metal
        Unknown sound: mob.irongolem.say
        Unknown sound: land.ladder
        Unknown sound: mob.attack
        Unknown sound: mob.attack
        Unknown sound: mob.piglin.attack
        Unknown sound: use.powder_snow
        Unknown sound: use.azalea
        Unknown sound: use.azalea_leaves
        Unknown sound: use.anvil
        Unknown sound: use.big_dripleaf
        Unknown sound: nearby_closest.warden
        Unknown sound: mob.zombie.converted_to_drowned
        Unknown sound: block.dried_ghast.hit
        Unknown sound: nearby_close.warden
        Unknown sound: mob.cow.death
        Unknown sound: nearby_closer.warden
    */

    public static void main(String[] args) throws Throwable {
        final ResourcePackStorage resourcePackStorage = Util.getClientResourcePacks(new File("C:\\XboxGames\\Minecraft for Windows\\Content\\data"));

        final JsonObject soundList = new JsonObject();
        final Set<String> soundsWithoutCategory = new HashSet<>();
        for (Map.Entry<String, SoundDefinitions.SoundDefinition> entry : resourcePackStorage.getSounds().soundDefinitions().entrySet()) {
            if (entry.getValue().category() == null) {
                soundsWithoutCategory.add(entry.getKey());
            } else {
                soundList.addProperty(entry.getKey(), entry.getValue().category());
            }
        }

        // Seems like mojang forgot to set the category for these sounds
        if (soundsWithoutCategory.contains("sign.ink_sac.use")) {
            soundsWithoutCategory.remove("sign.ink_sac.use");
            soundList.addProperty("sign.ink_sac.use", "block");
        }
        if (soundsWithoutCategory.contains("sign.dye.use")) {
            soundsWithoutCategory.remove("sign.dye.use");
            soundList.addProperty("sign.dye.use", "block");
        }
        if (soundsWithoutCategory.contains("music.overworld.forest")) {
            soundsWithoutCategory.remove("music.overworld.forest");
            soundList.addProperty("music.overworld.forest", "music");
        }
        if (soundsWithoutCategory.contains("record.tears")) {
            soundsWithoutCategory.remove("record.tears");
            soundList.addProperty("record.tears", "record");
        }
        if (soundsWithoutCategory.contains("record.lava_chicken")) {
            soundsWithoutCategory.remove("record.lava_chicken");
            soundList.addProperty("record.lava_chicken", "record");
        }
        if (soundsWithoutCategory.contains("game.player.attack.critical")) {
            soundsWithoutCategory.remove("game.player.attack.critical");
            soundList.addProperty("game.player.attack.critical", "player");
        }
        if (soundsWithoutCategory.contains("lt.reaction.fire")) {
            soundsWithoutCategory.remove("lt.reaction.fire");
            soundList.addProperty("lt.reaction.fire", "block");
        }
        if (soundsWithoutCategory.contains("elemconstruct.active")) {
            soundsWithoutCategory.remove("elemconstruct.active");
            soundList.addProperty("elemconstruct.active", "block");
        }
        // Check if there are any sounds without a category
        for (String s : soundsWithoutCategory) {
            throw new IllegalStateException("Sound without category: " + s);
        }

        final String json = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(GsonUtil.sort(soundList));
        Files.writeString(new File("sounds.json").toPath(), json);

        final Map<String, String> blockSounds = new TreeMap<>();
        for (Map.Entry<String, BlockDefinitions.BlockDefinition> blockEntry : resourcePackStorage.getBlocks().blocks().entrySet()) {
            if (blockEntry.getValue().sound() != null && !blockEntry.getValue().sound().isEmpty()) {
                blockSounds.put(blockEntry.getKey(), blockEntry.getValue().sound());
            }
        }

        final String json3 = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(blockSounds);
        Files.writeString(new File("block_sounds.json").toPath(), json3);

        final JsonObject levelSoundMappings = new JsonObject();
        Map<String, Map<String, SoundDefinitions.ConfiguredSound>> mapping = new HashMap<>();
        for (Map.Entry<String, SoundDefinitions.EventSound> entry : resourcePackStorage.getSounds().eventSounds().entrySet()) {
            mapping.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).put("", entry.getValue().sound());
        }

        for (Map.Entry<String, SoundDefinitions.EventSounds> entry : resourcePackStorage.getSounds().entitySounds().entrySet()) {
            for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().eventSounds().entrySet()) {
                mapping.computeIfAbsent(soundEntry.getKey(), k -> new HashMap<>()).put("entity:" + entry.getKey(), soundEntry.getValue());
            }
        }

        for (Map.Entry<String, SoundDefinitions.EventSounds> entry : resourcePackStorage.getSounds().blockSounds().entrySet()) {
            if (!blockSounds.containsValue(entry.getKey())) {
                System.out.println("Unknown block sound: " + entry.getKey());
                continue;
            }
            for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().eventSounds().entrySet()) {
                mapping.computeIfAbsent(soundEntry.getKey(), k -> new HashMap<>()).put("block:" + entry.getKey(), soundEntry.getValue());
            }
        }

        for (Map.Entry<String, Map<String, SoundDefinitions.ConfiguredSound>> entry : mapping.entrySet()) {
            SharedTypes_Legacy_LevelSoundEvent soundEvent = string2SoundEvent(entry.getKey());
            if (soundEvent != null) {
                if (levelSoundMappings.has(soundEvent.name())) {
                    System.out.println("Duplicate sound event: " + soundEvent.name() + " (" + entry.getKey() + ")");
                    continue;
                }
                JsonObject sounds = new JsonObject();
                for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().entrySet()) {
                    if (!soundList.has(soundEntry.getValue().sound())) {
                        System.out.println("Unknown sound: " + soundEntry.getValue().sound());
                        continue;
                    }
                    sounds.add(soundEntry.getKey(), soundEntry.getValue().toJson());
                }
                levelSoundMappings.add(soundEvent.name(), sounds);
            }
        }
        for (SharedTypes_Legacy_LevelSoundEvent soundEvent : SPECIAL_SOUNDS) {
            if (levelSoundMappings.has(soundEvent.name())) {
                System.out.println("Duplicate sound event: " + soundEvent.name());
                continue;
            }
            levelSoundMappings.add(soundEvent.name(), null);
        }

        final Map<String, JsonElement> sortedJson = new TreeMap<>();
        levelSoundMappings.entrySet().forEach(entry -> sortedJson.put(entry.getKey(), entry.getValue()));
        levelSoundMappings.entrySet().clear();
        sortedJson.forEach(levelSoundMappings::add);
        final String json2 = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create().toJson(levelSoundMappings);
        Files.writeString(new File("level_sound_event_mappings.json").toPath(), json2);
    }

    private static SharedTypes_Legacy_LevelSoundEvent string2SoundEvent(String s) {
       if (CANCELLED_SOUNDS.contains(s)) {
            return null;
        }
        final String original = s;

        s = s.replace(".", "");

        for (SharedTypes_Legacy_LevelSoundEvent soundEvent : SharedTypes_Legacy_LevelSoundEvent.values()) {
            if (s.equalsIgnoreCase(soundEvent.name())) {
                return soundEvent;
            } else if (s.replace("_", "").equalsIgnoreCase(soundEvent.name().replace("_", ""))) {
                return soundEvent;
            } else if (("ambient" + s).equalsIgnoreCase(soundEvent.name())) {
                return soundEvent;
            }
        }

        System.err.println("Unknown sound event: " + original);
        return null;
    }

}
