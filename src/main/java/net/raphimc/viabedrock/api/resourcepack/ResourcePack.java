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
package net.raphimc.viabedrock.api.resourcepack;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.resourcepack.content.Content;
import net.raphimc.viabedrock.api.resourcepack.content.ZipContent;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO: dependencies handling
// TODO: subpack handling
public class ResourcePack {

    private static final byte[] CONTENTS_JSON_ENCRYPTION_VERSION = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static final byte[] CONTENTS_JSON_ENCRYPTION_MAGIC = new byte[]{(byte) 0xFC, (byte) 0xB9, (byte) 0xCF, (byte) 0x9B};
    private static final Set<String> UNENCRYPTED_FILES = Set.of("manifest.json", "pack_manifest.json", "pack_icon.png", "pack_icon.jpg", "README.txt");

    private final Key key;
    private final String name;
    private final Content content;

    public ResourcePack(Content content) {
        try {
            if (!content.contains("manifest.json") && !content.contains("pack_manifest.json")) {
                // CDN packs are allowed to contain a single .zip file at the root
                final List<String> files = content.getFilesDeep("", "");
                if (files.size() == 1 && files.get(0).endsWith(".zip")) {
                    content = new ZipContent(content.get(files.get(0)));
                }
            }
            if (!content.contains("manifest.json") && !content.contains("pack_manifest.json")) {
                throw new IllegalStateException("Missing manifest.json");
            }
            final JsonObject manifestJson = content.contains("manifest.json") ? content.getJson("manifest.json") : content.getJson("pack_manifest.json");
            final int formatVersion = manifestJson.get("format_version").getAsInt();
            if (formatVersion < 1 || formatVersion > 3) {
                throw new IllegalStateException("Unsupported format version: " + formatVersion);
            }
            final JsonObject headerObj = manifestJson.getAsJsonObject("header");
            final UUID id = UUID.fromString(headerObj.get("uuid").getAsString());
            final String version;
            if (formatVersion >= 3) {
                version = headerObj.get("version").getAsString();
            } else {
                version = StreamSupport.stream(headerObj.getAsJsonArray("version").spliterator(), false).map(JsonElement::getAsString).collect(Collectors.joining("."));
            }
            this.key = new Key(id, version);
            this.name = headerObj.get("name").getAsString();
            /*if (formatVersion >= 2) { // Technically needed, but not feasible to implement currently
                final Semver minEngineVersion;
                if (formatVersion >= 3) {
                    minEngineVersion = new Semver(headerObj.get("min_engine_version").getAsString());
                } else {
                    minEngineVersion = new Semver(StreamSupport.stream(headerObj.getAsJsonArray("min_engine_version").spliterator(), false).map(JsonElement::getAsString).collect(Collectors.joining(".")));
                }
                if (minEngineVersion.isGreaterThan(ProtocolConstants.BEDROCK_VERSION_NAME)) {
                    throw new RuntimeException("Resource pack requires a newer game version: " + minEngineVersion + " > " + ProtocolConstants.BEDROCK_VERSION_NAME);
                }
            }*/
            this.content = content;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to parse resource pack", e);
        }
    }

    public void decryptContent(final byte[] contentKey, final String expectedContentId) {
        try {
            if (!this.content.contains("contents.json")) {
                throw new IllegalStateException("Missing contents.json");
            }
            final DataInputStream contents = new DataInputStream(new ByteArrayInputStream(this.content.get("contents.json")));
            contents.mark(256);
            final byte[] version = contents.readNBytes(4); // version
            if (!Arrays.equals(version, CONTENTS_JSON_ENCRYPTION_VERSION)) {
                throw new IllegalStateException("contents.json version mismatch: " + Arrays.toString(version) + " != " + Arrays.toString(CONTENTS_JSON_ENCRYPTION_VERSION));
            }
            final byte[] magic = contents.readNBytes(4); // magic
            if (!Arrays.equals(magic, CONTENTS_JSON_ENCRYPTION_MAGIC)) {
                throw new IllegalStateException("contents.json magic mismatch: " + Arrays.toString(magic) + " != " + Arrays.toString(CONTENTS_JSON_ENCRYPTION_MAGIC));
            }
            contents.skipNBytes(8); // ?
            final String contentId = new String(contents.readNBytes(contents.readUnsignedByte()), StandardCharsets.UTF_8); // content id
            if (!contentId.equalsIgnoreCase(expectedContentId)) {
                throw new IllegalStateException("contents.json content id mismatch: " + contentId + " != " + expectedContentId);
            }
            contents.reset();
            contents.skipNBytes(256); // header
            final Cipher aesCfb8 = Cipher.getInstance("AES/CFB8/NoPadding");
            aesCfb8.init(Cipher.DECRYPT_MODE, new SecretKeySpec(contentKey, "AES"), new IvParameterSpec(Arrays.copyOfRange(contentKey, 0, 16)));
            this.content.put("contents.json", aesCfb8.doFinal(contents.readAllBytes())); // encrypted contents.json

            final JsonObject contentsJson = this.content.getJson("contents.json");
            final JsonArray contentArray = contentsJson.getAsJsonArray("content");
            for (JsonElement element : contentArray) {
                final JsonObject contentItem = element.getAsJsonObject();
                if (!contentItem.has("key") || contentItem.get("key").isJsonNull()) {
                    continue;
                }
                final String path = contentItem.get("path").getAsString();
                if (!this.content.contains(path)) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing resource pack file: " + path);
                    continue;
                }
                if (UNENCRYPTED_FILES.contains(path)) {
                    continue;
                }

                final byte[] key = contentItem.get("key").getAsString().getBytes(StandardCharsets.ISO_8859_1);
                aesCfb8.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(Arrays.copyOfRange(key, 0, 16)));
                this.content.put(path, aesCfb8.doFinal(this.content.get(path)));
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to decrypt content", e);
        }
    }

    public boolean isContentEncrypted() {
        try {
            if (!this.content.contains("contents.json")) {
                return false;
            }
            final DataInputStream contents = new DataInputStream(new ByteArrayInputStream(this.content.get("contents.json")));
            final byte[] version = contents.readNBytes(4); // version
            if (!Arrays.equals(version, CONTENTS_JSON_ENCRYPTION_VERSION)) {
                return false;
            }
            final byte[] magic = contents.readNBytes(4); // magic
            if (!Arrays.equals(magic, CONTENTS_JSON_ENCRYPTION_MAGIC)) {
                return false;
            }
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public Key key() {
        return this.key;
    }

    public UUID id() {
        return this.key.id();
    }

    public String version() {
        return this.key.version();
    }

    public String name() {
        return this.name;
    }

    public Content content() {
        return this.content;
    }

    public record Key(UUID id, String version) {

        public static Key fromString(final String s) {
            final String[] parts = s.split("_", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid resource pack key: " + s);
            }
            return new Key(UUID.fromString(parts[0]), parts[1]);
        }

        @Override
        public String toString() {
            return this.id + "_" + this.version;
        }

    }

}
