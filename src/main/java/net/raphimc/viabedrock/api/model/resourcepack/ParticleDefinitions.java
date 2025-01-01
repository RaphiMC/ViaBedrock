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

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ParticleDefinitions {

    private final Map<String, ParticleDefinition> particles = new HashMap<>();

    public ParticleDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String particlePath : pack.content().getFilesDeep("particles/", ".json")) {
                try {
                    final JsonObject particleEffect = pack.content().getJson(particlePath).getAsJsonObject("particle_effect");
                    final String identifier = Key.namespaced(particleEffect.getAsJsonObject("description").get("identifier").getAsString());
                    final ParticleDefinition particleDefinition = new ParticleDefinition(identifier);
                    if (particleEffect.has("components")) {
                        final JsonObject components = particleEffect.getAsJsonObject("components");
                    }
                    this.particles.put(identifier, particleDefinition);
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse particle definition " + particlePath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public ParticleDefinition get(final String identifier) {
        return this.particles.get(identifier);
    }

    public Map<String, ParticleDefinition> particles() {
        return Collections.unmodifiableMap(this.particles);
    }

    public static class ParticleDefinition {

        private final String identifier;

        public ParticleDefinition(final String identifier) {
            this.identifier = identifier;
        }

        public String identifier() {
            return this.identifier;
        }

    }

}
