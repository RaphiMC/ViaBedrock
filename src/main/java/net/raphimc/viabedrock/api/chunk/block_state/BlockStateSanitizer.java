/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.chunk.block_state;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.api.util.NbtUtil;

import java.util.*;

public class BlockStateSanitizer {

    private final Map<String, Map<String, Set<Object>>> allowedPropertyValues = new HashMap<>();

    public BlockStateSanitizer(final List<BedrockBlockState> blockStates) {
        for (BedrockBlockState blockState : blockStates) {
            final String identifier = blockState.blockStateTag().<StringTag>get("name").getValue();
            final CompoundTag statesTag = blockState.blockStateTag().get("states");

            final Map<String, Set<Object>> propertyValues = this.allowedPropertyValues.computeIfAbsent(identifier, k -> new HashMap<>());
            for (Map.Entry<String, Tag> entry : statesTag.entrySet()) {
                propertyValues.computeIfAbsent(entry.getKey(), k -> new LinkedHashSet<>()).add(entry.getValue().getValue());
            }
        }
    }

    public void sanitize(final CompoundTag tag) {
        BedrockBlockState.sanitizeName(tag);

        final String identifier = tag.<StringTag>get("name").getValue();
        final Map<String, Set<Object>> propertyValues = this.allowedPropertyValues.get(identifier);
        if (propertyValues == null) {
            return;
        }

        CompoundTag statesTag = tag.get("states");
        if (statesTag == null) {
            if (propertyValues.isEmpty()) {
                return;
            } else {
                tag.put("states", statesTag = new CompoundTag());
            }
        }

        final Set<String> toRemove = new HashSet<>();
        for (Map.Entry<String, Tag> entry : statesTag.entrySet()) {
            final String property = entry.getKey();
            final Set<Object> allowedValues = propertyValues.get(property);
            if (allowedValues == null) {
                toRemove.add(property);
                continue;
            }

            if (!allowedValues.contains(entry.getValue().getValue())) {
                entry.setValue(NbtUtil.createTag(allowedValues.iterator().next()));
            }
        }

        for (String property : toRemove) {
            statesTag.remove(property);
        }

        for (Map.Entry<String, Set<Object>> entry : propertyValues.entrySet()) {
            final String property = entry.getKey();
            if (!statesTag.contains(property)) {
                statesTag.put(property, NbtUtil.createTag(entry.getValue().iterator().next()));
            }
        }
    }

}
