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
package net.raphimc.viabedrock.api.model.resourcepack;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

// https://wiki.bedrock.dev/concepts/sounds.html
public class SoundDefinitions {

    private final Map<String, SoundDefinition> soundDefinitions = new HashMap<>();
    private final Map<String, EventSound> eventSounds = new HashMap<>();
    private final Map<String, EventSounds> entitySounds = new HashMap<>();
    private final Map<String, EventSounds> blockSounds = new HashMap<>();

    public SoundDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            if (pack.content().contains("sounds/sound_definitions.json")) {
                try {
                    JsonObject soundDefinitions = pack.content().getJson("sounds/sound_definitions.json");
                    soundDefinitions = soundDefinitions.has("sound_definitions") ? soundDefinitions.getAsJsonObject("sound_definitions") : soundDefinitions;
                    for (Map.Entry<String, JsonElement> entry : soundDefinitions.entrySet()) {
                        final JsonObject entryData = entry.getValue().getAsJsonObject();
                        final String category = entryData.has("category") ? entryData.get("category").getAsString() : null;
                        final SoundDefinition soundDefinition = new SoundDefinition(entry.getKey(), category);
                        this.soundDefinitions.put(entry.getKey(), soundDefinition);
                    }
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse sound_definitions.json in pack " + pack.packId(), e);
                }
            }
            if (pack.content().contains("sounds.json")) {
                try {
                    final JsonObject sounds = pack.content().getJson("sounds.json");
                    if (sounds.has("individual_event_sounds")) {
                        final JsonObject events = sounds.getAsJsonObject("individual_event_sounds").getAsJsonObject("events");
                        for (Map.Entry<String, JsonElement> entry : events.entrySet()) {
                            final ConfiguredSound configuredSound = ConfiguredSound.fromJson(entry.getValue().getAsJsonObject());
                            if (configuredSound != null) {
                                this.eventSounds.put(entry.getKey(), new EventSound(entry.getKey(), configuredSound));
                            }
                        }
                    }
                    if (sounds.has("entity_sounds")) {
                        final JsonObject entitySounds = sounds.getAsJsonObject("entity_sounds");
                        final JsonObject entities = entitySounds.getAsJsonObject("entities");
                        this.mergeDefaults(entitySounds, entities);
                        this.parseEvents(entities, true, this.entitySounds);
                    }
                    if (sounds.has("block_sounds")) {
                        final JsonObject blockSounds = sounds.getAsJsonObject("block_sounds");
                        this.parseEvents(blockSounds, false, this.blockSounds);
                    }
                    if (sounds.has("interactive_sounds")) {
                        final JsonObject interactiveSounds = sounds.getAsJsonObject("interactive_sounds");
                        if (interactiveSounds.has("entity_sounds")) {
                            final JsonObject entitySounds = interactiveSounds.getAsJsonObject("entity_sounds");
                            final JsonObject entities = entitySounds.getAsJsonObject("entities");
                            this.mergeDefaults(entitySounds, entities);
                            // Entries can have different sounds for each block sound, but that's too much work and code for now, so we just modify the json to only have the default sound
                            for (Map.Entry<String, JsonElement> entityEntry : entities.entrySet()) {
                                final JsonObject events = entityEntry.getValue().getAsJsonObject().getAsJsonObject("events");
                                for (Map.Entry<String, JsonElement> eventEntry : events.entrySet()) {
                                    if (eventEntry.getValue().isJsonObject() && eventEntry.getValue().getAsJsonObject().has("default")) {
                                        events.add(eventEntry.getKey(), eventEntry.getValue().getAsJsonObject().get("default"));
                                    }
                                }
                            }
                            this.parseEvents(entities, true, this.entitySounds);
                        }
                        if (interactiveSounds.has("block_sounds")) {
                            this.parseEvents(interactiveSounds.getAsJsonObject("block_sounds"), false, this.blockSounds);
                        }
                    }
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse sounds.json in pack " + pack.packId(), e);
                }
            }
        }
    }

    private void parseEvents(final JsonObject sounds, final boolean namespace, final Map<String, EventSounds> soundMap) {
        for (Map.Entry<String, JsonElement> entry : sounds.entrySet()) {
            final JsonObject entity = entry.getValue().getAsJsonObject();
            if (!entity.has("events")) {
                continue;
            }

            final JsonElement volume = entity.has("volume") ? entity.get("volume") : null;
            final JsonElement pitch = entity.has("pitch") ? entity.get("pitch") : null;
            final Map<String, ConfiguredSound> eventSounds = new HashMap<>();
            for (Map.Entry<String, JsonElement> eventEntry : entity.getAsJsonObject("events").entrySet()) {
                if (eventEntry.getValue().isJsonPrimitive()) {
                    final JsonObject eventSound = new JsonObject();
                    eventSound.addProperty("sound", eventEntry.getValue().getAsString());
                    if (volume != null) {
                        eventSound.add("volume", volume);
                    }
                    if (pitch != null) {
                        eventSound.add("pitch", pitch);
                    }
                    final ConfiguredSound configuredSound = ConfiguredSound.fromJson(eventSound);
                    if (configuredSound != null) {
                        eventSounds.put(eventEntry.getKey(), configuredSound);
                    }
                } else {
                    final JsonObject eventSound = eventEntry.getValue().getAsJsonObject();
                    if (!eventSound.has("volume") && volume != null) {
                        eventSound.add("volume", volume);
                    }
                    if (!eventSound.has("pitch") && pitch != null) {
                        eventSound.add("pitch", pitch);
                    }
                    final ConfiguredSound configuredSound = ConfiguredSound.fromJson(eventSound);
                    if (configuredSound != null) {
                        eventSounds.put(eventEntry.getKey(), configuredSound);
                    }
                }
            }
            final String key = namespace ? Key.namespaced(entry.getKey()) : entry.getKey();
            if (soundMap.containsKey(key)) {
                soundMap.get(key).eventSounds().putAll(eventSounds);
            } else {
                soundMap.put(key, new EventSounds(key, eventSounds));
            }
        }
    }

    private void mergeDefaults(final JsonObject sounds, final JsonObject target) {
        if (sounds.has("defaults")) {
            final JsonObject defaults = sounds.getAsJsonObject("defaults");
            for (JsonElement value : target.asMap().values()) {
                JsonUtil.merge(value.getAsJsonObject(), defaults);
            }
        }
    }

    public Map<String, SoundDefinition> soundDefinitions() {
        return Collections.unmodifiableMap(this.soundDefinitions);
    }

    public Map<String, EventSound> eventSounds() {
        return Collections.unmodifiableMap(this.eventSounds);
    }

    public Map<String, EventSounds> entitySounds() {
        return Collections.unmodifiableMap(this.entitySounds);
    }

    public Map<String, EventSounds> blockSounds() {
        return Collections.unmodifiableMap(this.blockSounds);
    }

    public record SoundDefinition(String name, String category) {
    }

    public record ConfiguredSound(String sound, float minVolume, float maxVolume, float minPitch, float maxPitch) {

        public static ConfiguredSound fromJson(final JsonObject obj) {
            if (!obj.has("sound")) {
                return null;
            }
            final String sound = obj.get("sound").getAsString();
            if (sound.isEmpty()) {
                return null;
            }

            final float minVolume;
            final float maxVolume;
            if (obj.has("volume")) {
                if (obj.get("volume").isJsonArray()) {
                    minVolume = obj.get("volume").getAsJsonArray().get(0).getAsFloat();
                    maxVolume = obj.get("volume").getAsJsonArray().get(1).getAsFloat();
                } else {
                    minVolume = obj.get("volume").getAsFloat();
                    maxVolume = obj.get("volume").getAsFloat();
                }
            } else {
                minVolume = 1F;
                maxVolume = 1F;
            }
            final float minPitch;
            final float maxPitch;
            if (obj.has("pitch")) {
                if (obj.get("pitch").isJsonArray()) {
                    minPitch = obj.get("pitch").getAsJsonArray().get(0).getAsFloat();
                    maxPitch = obj.get("pitch").getAsJsonArray().get(1).getAsFloat();
                } else {
                    minPitch = obj.get("pitch").getAsFloat();
                    maxPitch = obj.get("pitch").getAsFloat();
                }
            } else {
                minPitch = 1F;
                maxPitch = 1F;
            }
            return new ConfiguredSound(sound, minVolume, maxVolume, minPitch, maxPitch);
        }

        public JsonObject toJson() {
            final JsonObject obj = new JsonObject();
            obj.addProperty("sound", this.sound);
            if (this.minVolume != 1F || this.maxVolume != 1F) {
                if (this.minVolume == this.maxVolume) {
                    obj.addProperty("volume", this.minVolume);
                } else {
                    final JsonArray volumeArray = new JsonArray();
                    volumeArray.add(this.minVolume);
                    volumeArray.add(this.maxVolume);
                    obj.add("volume", volumeArray);
                }
            }
            if (this.minPitch != 1F || this.maxPitch != 1F) {
                if (this.minPitch == this.maxPitch) {
                    obj.addProperty("pitch", this.minPitch);
                } else {
                    final JsonArray pitchArray = new JsonArray();
                    pitchArray.add(this.minPitch);
                    pitchArray.add(this.maxPitch);
                    obj.add("pitch", pitchArray);
                }
            }
            return obj;
        }

    }

    public record EventSound(String event, ConfiguredSound sound) {
    }

    public record EventSounds(String identifier, Map<String, ConfiguredSound> eventSounds) {
    }

}
