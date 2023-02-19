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
package net.raphimc.viabedrock.protocol.data;

import com.google.common.collect.Maps;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BlockState {

    public static final BlockState AIR = new BlockState("air", Collections.emptyMap());
    public static final BlockState STONE = new BlockState("stone", Collections.emptyMap());
    public static final BlockState INFO_UPDATE = new BlockState("info_update", Collections.emptyMap());

    private final String namespace;
    private final String identifier;
    private final Map<String, String> properties;

    public BlockState(final String identifier, final Map<String, String> properties) {
        this("minecraft", identifier, properties);
    }

    public BlockState(final String namespace, final String identifier, final Map<String, String> properties) {
        if (namespace == null || identifier == null) {
            throw new IllegalArgumentException("BlockState namespace or identifier was null: " + namespace + ":" + identifier);
        }

        this.namespace = namespace.toLowerCase(Locale.ROOT);
        this.identifier = identifier.toLowerCase(Locale.ROOT);
        this.properties = Collections.unmodifiableMap(properties);
    }

    public static BlockState fromString(final String string) {
        String namespace = "minecraft";
        String identifier;
        String propertiesString = null;

        if (string.contains("[") && string.endsWith("]")) {
            final String[] split = string.split("\\[", 2);
            identifier = split[0];
            propertiesString = split[1].substring(0, split[1].length() - 1);
        } else {
            identifier = string;
        }

        if (identifier.contains(":")) {
            final String[] namespaceAndIdentifier = identifier.split(":", 2);
            namespace = namespaceAndIdentifier[0];
            identifier = namespaceAndIdentifier[1];
        }

        if (propertiesString != null) {
            final Map<String, String> properties = Maps.newHashMap();
            final String[] propertiesSplit;
            if (propertiesString.contains(",")) {
                propertiesSplit = propertiesString.split(",");
            } else if (!propertiesString.isEmpty()) {
                propertiesSplit = new String[]{propertiesString};
            } else {
                propertiesSplit = new String[0];
            }
            for (final String property : propertiesSplit) {
                final String[] split = property.split("=", 2);
                properties.put(split[0], split[1]);
            }
            return new BlockState(namespace, identifier, properties);
        } else {
            return new BlockState(namespace, identifier, Collections.emptyMap());
        }
    }

    public static BlockState fromNbt(final CompoundTag tag) {
        String namespace = "minecraft";
        String identifier = tag.<StringTag>get("name").getValue();

        if (identifier.contains(":")) {
            final String[] namespaceAndIdentifier = identifier.split(":", 2);
            namespace = namespaceAndIdentifier[0];
            identifier = namespaceAndIdentifier[1];
        }

        if (tag.get("states") instanceof CompoundTag) {
            final Map<String, String> properties = Maps.newHashMap();
            final CompoundTag statesTag = tag.get("states");
            for (final String key : statesTag.getValue().keySet()) {
                properties.put(key, statesTag.get(key).getValue().toString());
            }
            return new BlockState(namespace, identifier, properties);
        } else {
            return new BlockState(namespace, identifier, Collections.emptyMap());
        }
    }

    public BlockState withNamespace(final String namespace) {
        return new BlockState(namespace, this.identifier, this.properties);
    }

    public BlockState withIdentifier(final String identifier) {
        return new BlockState(this.namespace, identifier, this.properties);
    }

    public BlockState withNamespacedIdentifier(final String namespacedIdentifier) {
        if (namespacedIdentifier.contains(":")) {
            final String[] namespaceAndIdentifier = namespacedIdentifier.split(":", 2);
            return new BlockState(namespaceAndIdentifier[0], namespaceAndIdentifier[1], this.properties);
        } else {
            return new BlockState(this.namespace, namespacedIdentifier, this.properties);
        }
    }

    public BlockState withProperty(final String key, final String value) {
        final Map<String, String> newProperties = Maps.newHashMap(this.properties);
        newProperties.put(key, value);
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public BlockState withProperties(final Map<String, String> properties) {
        final Map<String, String> newProperties = Maps.newHashMap(this.properties);
        newProperties.putAll(properties);
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public BlockState withoutProperties(final String... keys) {
        final Map<String, String> newProperties = Maps.newHashMap(this.properties);
        for (final String key : keys) {
            newProperties.remove(key);
        }
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getNamespacedIdentifier() {
        return this.namespace + ":" + this.identifier;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockState that = (BlockState) o;
        return Objects.equals(namespace, that.namespace) && Objects.equals(identifier, that.identifier) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, identifier, properties);
    }

    @Override
    public String toString() {
        return "BlockState{" +
                "namespace='" + namespace + '\'' +
                ", identifier='" + identifier + '\'' +
                ", properties=" + properties +
                '}';
    }

}
