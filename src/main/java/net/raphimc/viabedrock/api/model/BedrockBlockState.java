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
package net.raphimc.viabedrock.api.model;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BedrockBlockState extends BlockState {

    public static final BlockState AIR = new BlockState("air", Collections.emptyMap());
    public static final BlockState STONE = new BlockState("stone", Collections.singletonMap("stone_type", "stone"));
    public static final BlockState INFO_UPDATE = new BlockState("info_update", Collections.emptyMap());

    private final CompoundTag blockStateTag;

    private BedrockBlockState(final String namespace, final String identifier, final Map<String, String> properties, final CompoundTag blockStateTag) {
        super(namespace, identifier, properties);

        this.blockStateTag = blockStateTag;
    }

    public static BedrockBlockState fromNbt(final CompoundTag tag) {
        sanitizeName(tag);

        final String[] namespaceAndIdentifier = tag.getStringTag("name").getValue().split(":", 2);
        final Map<String, String> properties = new HashMap<>();
        if (tag.get("states") instanceof CompoundTag) {
            for (Map.Entry<String, Tag> entry : tag.getCompoundTag("states").getValue().entrySet()) {
                properties.put(entry.getKey(), entry.getValue().getValue().toString());
            }
        }

        return new BedrockBlockState(namespaceAndIdentifier[0], namespaceAndIdentifier[1], properties, tag);
    }

    public static void sanitizeName(final CompoundTag tag) {
        final StringTag name = tag.getStringTag("name");
        String namespace = "minecraft";
        String identifier = name.getValue();
        if (identifier.contains(":")) {
            final String[] namespaceAndIdentifier = identifier.split(":", 2);
            namespace = namespaceAndIdentifier[0];
            identifier = namespaceAndIdentifier[1].toLowerCase(Locale.ROOT);
        } else {
            identifier = identifier.toLowerCase(Locale.ROOT);
        }
        name.setValue(namespace + ":" + identifier);
    }

    public CompoundTag blockStateTag() {
        return this.blockStateTag;
    }

}
