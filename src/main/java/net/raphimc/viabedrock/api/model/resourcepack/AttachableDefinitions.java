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
package net.raphimc.viabedrock.api.model.resourcepack;

import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import org.oryxel.cube.model.bedrock.data.BedrockAttachableData;
import org.oryxel.cube.parser.bedrock.data.BedrockAttachableSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

// https://wiki.bedrock.dev/items/attachables.html
public class AttachableDefinitions {

    private final Map<String, AttachableDefinition> attachables = new HashMap<>();

    public AttachableDefinitions(final ResourcePacksStorage resourcePacksStorage) {
        for (ResourcePack pack : resourcePacksStorage.getPackStackBottomToTop()) {
            for (String attachablePath : pack.content().getFilesDeep("attachables/", ".json")) {
                try {
                    final BedrockAttachableData attachableData = BedrockAttachableSerializer.deserialize(pack.content().getString(attachablePath));
                    final String identifier = Key.namespaced(attachableData.identifier());
                    this.attachables.put(identifier, new AttachableDefinition(identifier, attachableData));
                } catch (Throwable e) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Failed to parse attachable definition " + attachablePath + " in pack " + pack.packId(), e);
                }
            }
        }
    }

    public Map<String, AttachableDefinition> attachables() {
        return Collections.unmodifiableMap(this.attachables);
    }

    public record AttachableDefinition(String identifier, BedrockAttachableData attachableData) {
    }

}
