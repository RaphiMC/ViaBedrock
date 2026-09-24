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

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.resourcepack.definition.BlockDefinitions;
import net.raphimc.viabedrock.api.resourcepack.definition.SoundDefinitions;
import net.raphimc.viabedrock.protocol.storage.ResourcePackStorage;
import net.raphimc.viabedrock.tool.ToolArgs;
import net.raphimc.viabedrock.tool.ToolPaths;
import net.raphimc.viabedrock.util.Util;

import java.util.*;

public final class BedrockSoundListGenerator {

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

    private static final Set<String> SPECIAL_SOUNDS = Set.of(
        // Mapped in code
        "record.null",
        "note",
        // Those seem to do nothing
        "item.fizz",
        "mob.warning.baby",
        "haggle.idle",
        "default",
        "scared",
        "jump.prevent",
        "undefined"
    );

    public static void main(final String[] args) throws Throwable {
        final ToolArgs toolArgs = ToolArgs.parse(args);
        final ResourcePackStorage resourcePackStorage = Util.getClientResourcePacks(ToolPaths.clientDataDir(toolArgs));

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

        ToolPaths.writeJson(ToolPaths.BEDROCK_DATA.resolve("sounds.json"), GsonUtil.sort(soundList));

        final Map<String, String> blockSounds = new TreeMap<>();
        for (Map.Entry<String, BlockDefinitions.BlockDefinition> blockEntry : resourcePackStorage.getBlocks().blocks().entrySet()) {
            if (blockEntry.getValue().sound() != null && !blockEntry.getValue().sound().isEmpty()) {
                blockSounds.put(blockEntry.getKey(), blockEntry.getValue().sound());
            }
        }

        final JsonObject blockSoundsJson = new JsonObject();
        blockSounds.forEach(blockSoundsJson::addProperty);
        ToolPaths.writeJson(ToolPaths.BEDROCK_DATA.resolve("block_sounds.json"), blockSoundsJson);

        final JsonObject levelSoundMappings = new JsonObject();
        final Map<String, Map<String, SoundDefinitions.ConfiguredSound>> mapping = new HashMap<>();
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
            if (CANCELLED_SOUNDS.contains(entry.getKey())) {
                continue;
            }

            if (levelSoundMappings.has(entry.getKey())) {
                System.out.println("Duplicate sound event: " + entry.getKey());
                continue;
            }
            final JsonObject sounds = new JsonObject();
            for (Map.Entry<String, SoundDefinitions.ConfiguredSound> soundEntry : entry.getValue().entrySet()) {
                if (!soundList.has(soundEntry.getValue().sound())) {
                    System.out.println("Unknown sound: " + soundEntry.getValue().sound());
                    continue;
                }
                sounds.add(soundEntry.getKey(), soundEntry.getValue().toJson());
            }
            levelSoundMappings.add(entry.getKey(), sounds);

        }
        for (String soundEvent : SPECIAL_SOUNDS) {
            if (levelSoundMappings.has(soundEvent)) {
                System.out.println("Duplicate sound event: " + soundEvent);
                continue;
            }
            levelSoundMappings.add(soundEvent, null);
        }

        final Map<String, JsonElement> sortedJson = new TreeMap<>();
        levelSoundMappings.entrySet().forEach(entry -> sortedJson.put(entry.getKey(), entry.getValue()));
        levelSoundMappings.entrySet().clear();
        sortedJson.forEach(levelSoundMappings::add);
        ToolPaths.writeJson(ToolPaths.BEDROCK_DATA.resolve("level_sound_event_mappings.json"), levelSoundMappings, true);
    }

    private BedrockSoundListGenerator() {
    }

}
