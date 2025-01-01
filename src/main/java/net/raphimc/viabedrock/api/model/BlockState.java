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

import java.util.*;

public class BlockState {

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

        this.namespace = namespace;
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
            final Map<String, String> properties = new HashMap<>();
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
        final Map<String, String> newProperties = new HashMap<>(this.properties);
        newProperties.put(key, value);
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public BlockState withProperties(final Map<String, String> properties) {
        final Map<String, String> newProperties = new HashMap<>(this.properties);
        newProperties.putAll(properties);
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public BlockState withoutProperties(final String... keys) {
        final Map<String, String> newProperties = new HashMap<>(this.properties);
        for (final String key : keys) {
            newProperties.remove(key);
        }
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public BlockState replaceProperty(final String key, final String value) {
        if (!this.properties.containsKey(key)) return this;

        return this.withProperty(key, value);
    }

    public BlockState replaceProperties(final Map<String, String> properties) {
        final Map<String, String> newProperties = new HashMap<>(this.properties);
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            if (newProperties.containsKey(entry.getKey())) {
                newProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return new BlockState(this.namespace, this.identifier, newProperties);
    }

    public String toBlockStateString() {
        return this.toBlockStateString(false);
    }

    public String toBlockStateString(final boolean sorted) {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.namespace).append(":").append(this.identifier);
        if (!this.properties.isEmpty()) {
            builder.append("[");
            boolean first = true;
            for (final Map.Entry<String, String> entry : (sorted ? new TreeMap<>(this.properties) : this.properties).entrySet()) {
                if (!first) {
                    builder.append(",");
                }
                builder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            builder.append("]");
        }
        return builder.toString();
    }

    public String namespace() {
        return this.namespace;
    }

    public String identifier() {
        return this.identifier;
    }

    public String namespacedIdentifier() {
        return this.namespace + ":" + this.identifier;
    }

    public Map<String, String> properties() {
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockState that)) return false;
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
