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
package net.raphimc.viabedrock.tool.mapping;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Key;
import net.raphimc.viabedrock.api.model.BlockState;

import java.util.*;

/**
 * Checks the java item every item mapping points at.
 * <p>
 * Two things go wrong in an update. A new bedrock item gets mapped to whatever was closest at the time, and then java
 * gains the real thing and nobody comes back to it. Or a whole family gets copied from an older one and only the
 * bedrock side of the copy is renamed, which is how every {@code poplar_*} item ended up on {@code pale_oak_*}.
 */
public class ItemMappingChecker {

    private final MappingAssets assets;
    private final Set<String> javaItems;

    public ItemMappingChecker(final MappingAssets assets) {
        this.assets = assets;
        this.javaItems = assets.javaNamespaced("items");
    }

    public List<ItemMappingFix> run() {
        final JsonObject mappings = this.assets.json("custom/item_mappings.json");
        final Map<String, String> javaIdByBedrockItem = new LinkedHashMap<>();
        final Map<String, String> ownerByJavaId = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            final String javaId = primaryJavaId(entry.getValue().getAsJsonObject());
            if (javaId != null) {
                javaIdByBedrockItem.put(entry.getKey(), javaId);
            }
            for (String usedJavaId : allJavaIds(entry.getValue().getAsJsonObject())) {
                ownerByJavaId.putIfAbsent(usedJavaId, entry.getKey());
            }
        }

        final List<ItemMappingFix> fixes = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            final String bedrockIdentifier = entry.getKey();
            final JsonObject definition = entry.getValue().getAsJsonObject();
            final String container = definition.has("block") ? "block" : "meta";

            for (Map.Entry<String, JsonElement> mapping : definition.getAsJsonObject(container).entrySet()) {
                if (!mapping.getValue().isJsonObject() || !mapping.getValue().getAsJsonObject().has("java_id")) {
                    continue;
                }
                final String currentJavaId = Key.namespaced(mapping.getValue().getAsJsonObject().get("java_id").getAsString());
                final String path = container + "/" + mapping.getKey();

                final ItemMappingFix identityFix = this.checkIdentity(bedrockIdentifier, container, mapping.getKey(), path, currentJavaId, ownerByJavaId);
                if (identityFix != null) {
                    fixes.add(identityFix);
                    continue;
                }
                final ItemMappingFix analogyFix = this.checkAnalogy(bedrockIdentifier, path, currentJavaId, javaIdByBedrockItem);
                if (analogyFix != null) {
                    fixes.add(analogyFix);
                }
            }
        }
        fixes.sort(Comparator.comparing(ItemMappingFix::key));
        return fixes;
    }

    /**
     * Java has an item of exactly this name but the mapping points elsewhere. For block items the name to compare
     * against is the block state's, for meta items only the default mapping is comparable, since the other meta
     * values are variants which are meant to point somewhere else.
     */
    private ItemMappingFix checkIdentity(final String bedrockIdentifier, final String container, final String mappingKey, final String path, final String currentJavaId, final Map<String, String> ownerByJavaId) {
        final String identity;
        if (container.equals("block")) {
            identity = BlockState.fromString(mappingKey).namespacedIdentifier();
        } else if (mappingKey.isEmpty()) {
            identity = Key.namespaced(bedrockIdentifier);
        } else {
            return null;
        }

        if (!this.javaItems.contains(identity) || currentJavaId.equals(identity)) {
            return null;
        }
        // Something else already maps to it, so the name being free is an illusion: bedrock's undyed_shulker_box
        // owns java's shulker_box, and arrow meta 0 owns java's arrow
        final String owner = ownerByJavaId.get(identity);
        if (owner != null) {
            return null;
        }
        return new ItemMappingFix(bedrockIdentifier, path, currentJavaId, identity, "java has an item called " + identity + " and nothing else maps to it");
    }

    /**
     * The mapping points at the java item another bedrock item owns, and renaming it the way the two bedrock items
     * differ lands on an item java actually has. That is the signature of a family copied from an older one where
     * only the bedrock side was renamed, which is how every {@code poplar_*} item ended up on {@code pale_oak_*}.
     */
    private ItemMappingFix checkAnalogy(final String bedrockIdentifier, final String path, final String currentJavaId, final Map<String, String> javaIdByBedrockItem) {
        for (Map.Entry<String, String> sibling : javaIdByBedrockItem.entrySet()) {
            if (sibling.getKey().equals(bedrockIdentifier) || !sibling.getValue().equals(currentJavaId)) {
                continue;
            }
            if (!sharesFamily(sibling.getKey(), bedrockIdentifier)) {
                continue;
            }
            final String suggested = Similarity.rewriteIdentifier(sibling.getKey(), bedrockIdentifier, currentJavaId);
            if (suggested == null || suggested.equals(currentJavaId) || !this.javaItems.contains(suggested)) {
                continue;
            }
            return new ItemMappingFix(bedrockIdentifier, path, currentJavaId, suggested,
                "this is " + sibling.getKey() + "'s java item, renaming it the same way gives " + suggested);
        }
        return null;
    }

    /**
     * Two identifiers belong to the same family when most of the shorter one is a shared trailing word. Without the
     * second half, {@code white_shulker_box} and {@code shulker_box} look related through {@code _box} alone.
     */
    private static boolean sharesFamily(final String a, final String b) {
        final int suffix = Similarity.commonSuffixLength(a, b);
        final int shorter = Math.min(stripNamespace(a).length(), stripNamespace(b).length());
        return suffix >= 6 && suffix * 2 >= shorter;
    }

    private static String stripNamespace(final String identifier) {
        final int namespace = identifier.indexOf(':');
        return namespace == -1 ? identifier : identifier.substring(namespace + 1);
    }

    private static List<String> allJavaIds(final JsonObject definition) {
        final JsonObject container = definition.has("block") ? definition.getAsJsonObject("block") : definition.getAsJsonObject("meta");
        final List<String> javaIds = new ArrayList<>();
        for (Map.Entry<String, JsonElement> mapping : container.entrySet()) {
            if (mapping.getValue().isJsonObject() && mapping.getValue().getAsJsonObject().has("java_id")) {
                javaIds.add(Key.namespaced(mapping.getValue().getAsJsonObject().get("java_id").getAsString()));
            }
        }
        return javaIds;
    }

    private static String primaryJavaId(final JsonObject definition) {
        final JsonObject container = definition.has("block") ? definition.getAsJsonObject("block") : definition.getAsJsonObject("meta");
        for (Map.Entry<String, JsonElement> mapping : container.entrySet()) {
            if (mapping.getValue().isJsonObject() && mapping.getValue().getAsJsonObject().has("java_id")) {
                return Key.namespaced(mapping.getValue().getAsJsonObject().get("java_id").getAsString());
            }
        }
        return null;
    }

}
