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
import com.viaversion.viaversion.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.util.JsonUtil;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.PackType;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ResourcePack {

    private static final byte[] CONTENTS_JSON_ENCRYPTED_MAGIC = new byte[]{(byte) 0xFC, (byte) 0xB9, (byte) 0xCF, (byte) 0x9B};

    private UUID packId;
    private String version;
    private byte[] contentKey;
    private final String subPackName;
    private final String contentId;
    private final boolean hasScripts;
    private final boolean isAddonPack;
    private final boolean raytracingCapable;

    // Non HTTP resource pack downloading
    private byte[] hash;
    private boolean premium;
    private PackType type;

    // HTTP resource pack downloading
    private URL cdnUrl;

    private int maxChunkSize;
    private boolean[] receivedChunks;
    private byte[] compressedData;
    private Content content;

    public ResourcePack(final UUID packId, final String version, final byte[] contentKey, final String subPackName, final String contentId, final boolean hasScripts, final boolean isAddonPack, final boolean raytracingCapable, final URL cdnUrl, final long compressedSize, final PackType type) {
        this.packId = packId;
        this.version = version;
        this.contentKey = contentKey;
        this.subPackName = subPackName;
        this.contentId = contentId;
        this.hasScripts = hasScripts;
        this.isAddonPack = isAddonPack;
        this.raytracingCapable = raytracingCapable;
        this.cdnUrl = cdnUrl;
        this.compressedData = new byte[(int) compressedSize];
        this.type = type;
    }

    public boolean processDataChunk(final int chunkIndex, final byte[] data) throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (this.receivedChunks[chunkIndex]) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received duplicate resource pack chunk data: " + this.packId);
            return false;
        }

        final int offset = chunkIndex * this.maxChunkSize;
        if (offset + data.length > this.compressedData.length) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Received resource pack chunk data with invalid offset: " + this.packId);
            return false;
        }
        System.arraycopy(data, 0, this.compressedData, offset, data.length);
        this.receivedChunks[chunkIndex] = true;

        if (this.hasReceivedAllChunks()) {
            this.decompressAndDecrypt();
            return true;
        }

        return false;
    }

    public boolean isDecompressed() {
        return this.compressedData == null;
    }

    public UUID packId() {
        return this.packId;
    }

    public String version() {
        return this.version;
    }

    public byte[] contentKey() {
        return this.contentKey;
    }

    public void setContentKey(final byte[] contentKey) {
        this.contentKey = contentKey;
    }

    public String subPackName() {
        return this.subPackName;
    }

    public String contentId() {
        return this.contentId;
    }

    public boolean hasScripts() {
        return this.hasScripts;
    }

    public boolean isAddonPack() {
        return this.isAddonPack;
    }

    public boolean raytracingCapable() {
        return this.raytracingCapable;
    }

    public void setHash(final byte[] hash) {
        this.hash = hash;
    }

    public boolean premium() {
        return this.premium;
    }

    public void setPremium(final boolean premium) {
        this.premium = premium;
    }

    public PackType type() {
        return this.type;
    }

    public void setType(final PackType type) {
        this.type = type;
    }

    public URL cdnUrl() {
        return this.cdnUrl;
    }

    public void setCdnUrl(final URL cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

    public int compressedDataLength() {
        if (this.compressedData == null) {
            return 0;
        }

        return this.compressedData.length;
    }

    public void setCompressedDataLength(final int length, final int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        this.receivedChunks = new boolean[MathUtil.ceil((float) length / maxChunkSize)];
        this.compressedData = new byte[length];
    }

    public Content content() {
        if (!this.isDecompressed()) {
            throw new IllegalStateException("Pack is not decompressed");
        }

        return this.content;
    }

    private void decompressAndDecrypt() throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (this.hash != null) {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final byte[] hash = sha256.digest(this.compressedData);
            if (!Arrays.equals(hash, this.hash)) {
                throw new IllegalStateException("Resource pack hash mismatch: " + this.packId);
            }
        }

        this.content = new Content(this.compressedData);
        this.compressedData = null;

        if (!this.content.contains("manifest.json") && !this.content.contains("pack_manifest.json") && this.cdnUrl != null && this.content.size() == 1) {
            // CDN packs are allowed to contain a single .zip file at the root
            final String key = this.content.content.keySet().iterator().next();
            if (key.endsWith(".zip")) {
                this.content = new Content(this.content.get(key));
            }
        }
        if (!this.content.contains("manifest.json") && !this.content.contains("pack_manifest.json")) {
            // Bedrock allows resource packs to contain a single subfolder at the root
            for (String path : new HashSet<>(this.content.content.keySet())) {
                if (path.contains("/")) {
                    this.content.put(path.substring(path.indexOf('/') + 1), this.content.content.remove(path));
                }
            }
        }
        if (!this.content.contains("manifest.json") && !this.content.contains("pack_manifest.json")) {
            throw new IllegalStateException("Missing manifest.json in resource pack: " + this.packId);
        }

        if (this.contentKey.length != 0) {
            if (!this.content.contains("contents.json")) {
                throw new IllegalStateException("Missing contents.json in resource pack: " + this.packId);
            }
            final Cipher aesCfb8 = Cipher.getInstance("AES/CFB8/NoPadding");
            aesCfb8.init(Cipher.DECRYPT_MODE, new SecretKeySpec(this.contentKey, "AES"), new IvParameterSpec(Arrays.copyOfRange(this.contentKey, 0, 16)));
            final ByteBuf contents = Unpooled.wrappedBuffer(this.content.get("contents.json"));
            contents.skipBytes(4); // version
            final byte[] magic = new byte[4];
            contents.readBytes(magic); // magic
            if (!Arrays.equals(magic, CONTENTS_JSON_ENCRYPTED_MAGIC)) {
                throw new IllegalStateException("contents.json magic mismatch: " + Arrays.toString(CONTENTS_JSON_ENCRYPTED_MAGIC) + " != " + Arrays.toString(magic));
            }
            contents.readerIndex(16);
            final short contentIdLength = contents.readUnsignedByte(); // content id length
            final byte[] contentIdBytes = new byte[contentIdLength];
            contents.readBytes(contentIdBytes); // content id
            final String contentId = new String(contentIdBytes, StandardCharsets.UTF_8);
            if (!this.contentId.equalsIgnoreCase(contentId)) {
                throw new IllegalStateException("contents.json contentId mismatch: " + this.contentId + " != " + contentId);
            }
            contents.readerIndex(256);
            final byte[] encryptedContents = new byte[contents.readableBytes()];
            contents.readBytes(encryptedContents); // encrypted contents.json
            this.content.put("contents.json", aesCfb8.doFinal(encryptedContents));

            final JsonObject contentsJson = this.content.getJson("contents.json");
            final JsonArray contentArray = contentsJson.getAsJsonArray("content");
            for (JsonElement element : contentArray) {
                final JsonObject contentItem = element.getAsJsonObject();
                if (!contentItem.has("key") || contentItem.get("key").isJsonNull()) continue;
                final String key = contentItem.get("key").getAsString();
                final String path = contentItem.get("path").getAsString();
                if (!this.content.contains(path)) {
                    ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Missing resource pack file: " + path);
                    continue;
                }
                switch (path) {
                    case "manifest.json", "pack_manifest.json", "pack_icon.png", "README.txt" -> {
                        continue;
                    }
                }

                final byte[] encryptedData = this.content.get(path);
                final byte[] keyBytes = key.getBytes(StandardCharsets.ISO_8859_1);
                aesCfb8.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0, 16)));
                this.content.put(path, aesCfb8.doFinal(encryptedData));
            }
        }

        final JsonObject manifestJson = this.content.contains("manifest.json") ? this.content.getJson("manifest.json") : this.content.getJson("pack_manifest.json");
        final int formatVersion = manifestJson.get("format_version").getAsInt();
        if (formatVersion != 1 && formatVersion != 2) {
            throw new IllegalStateException("Unsupported resource pack format version: " + formatVersion);
        }
        final JsonObject headerObj = manifestJson.getAsJsonObject("header");
        final UUID packId = UUID.fromString(headerObj.get("uuid").getAsString());
        if (this.packId == null) {
            this.packId = packId;
        } else if (!this.packId.equals(packId)) {
            throw new IllegalStateException("manifest.json packId mismatch: " + this.packId + " != " + packId);
        }
        final JsonArray versionArray = headerObj.getAsJsonArray("version");
        final StringBuilder version = new StringBuilder();
        for (JsonElement digit : versionArray) {
            version.append(digit.getAsString()).append(".");
        }
        version.deleteCharAt(version.length() - 1);
        if (this.version == null) {
            this.version = version.toString();
        } else if (!this.version.contentEquals(version)) {
            throw new IllegalStateException("manifest.json version mismatch: " + this.version + " != " + version);
        }
    }

    private boolean hasReceivedAllChunks() {
        for (final boolean receivedChunk : this.receivedChunks) {
            if (!receivedChunk) {
                return false;
            }
        }
        return true;
    }

    public static class Content {

        private final Map<String, byte[]> content;
        private final Map<String, Map<String, String>> langCache;

        public Content() {
            this(false);
        }

        public Content(final boolean concurrent) {
            if (concurrent) {
                this.content = new ConcurrentHashMap<>();
                this.langCache = new ConcurrentHashMap<>();
            } else {
                this.content = new HashMap<>();
                this.langCache = new HashMap<>();
            }
        }

        public Content(final byte[] zipData) throws IOException {
            this(false);

            final ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipData));
            ZipEntry zipEntry;
            int len;
            final byte[] buf = new byte[4096];
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) continue;
                while ((len = zipInputStream.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                this.content.put(zipEntry.getName(), baos.toByteArray());
                baos.reset();
            }
        }

        public List<String> getFilesShallow(final String path, final String extension) {
            return this.content.keySet().stream().filter(file -> file.startsWith(path) && !file.substring(path.length()).contains("/") && file.endsWith(extension)).collect(Collectors.toList());
        }

        public List<String> getFilesDeep(final String path, final String extension) {
            return this.content.keySet().stream().filter(file -> file.startsWith(path) && file.endsWith(extension)).collect(Collectors.toList());
        }

        public String getFullPath(final String shortNamePath, final String... extensions) {
            if (this.contains(shortNamePath)) {
                return shortNamePath;
            }
            for (final String extension : extensions) {
                final String path = shortNamePath + "." + extension;
                if (this.contains(path)) {
                    return path;
                }
            }
            return null;
        }

        public boolean contains(final String path) {
            return this.content.containsKey(path);
        }

        public byte[] get(final String path) {
            return this.content.get(path);
        }

        public boolean put(final String path, final byte[] data) {
            return this.content.put(path, data) != null;
        }

        public String getString(final String path) {
            final byte[] bytes = this.get(path);
            if (bytes == null) {
                return null;
            }

            return new String(bytes, StandardCharsets.UTF_8);
        }

        public boolean putString(final String path, final String string) {
            return this.put(path, string.getBytes(StandardCharsets.UTF_8));
        }

        public List<String> getLines(final String path) {
            final String string = this.getString(path);
            if (string == null) {
                return null;
            }

            return List.of(string.split("\\n"));
        }

        public boolean putLines(final String path, final List<String> lines) {
            return this.putString(path, String.join("\\n", lines));
        }

        public Map<String, String> getLang(final String path) {
            return this.langCache.computeIfAbsent(path, k -> {
                final List<String> lines = this.getLines(k);
                return Collections.unmodifiableMap(lines.stream()
                        .filter(line -> !line.startsWith("##"))
                        .filter(line -> line.contains("="))
                        .map(line -> line.contains("##") ? line.substring(0, line.indexOf("##")) : line)
                        .map(String::trim)
                        .map(line -> line.split("=", 2))
                        .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1], (o, n) -> n)));
            });
        }

        public JsonObject getJson(final String path) {
            final String string = this.getString(path);
            if (string == null) {
                return null;
            }

            return GsonUtil.getGson().fromJson(string.trim(), JsonObject.class);
        }

        public JsonObject getSortedJson(final String path) {
            return JsonUtil.sort(this.getJson(path), Comparator.naturalOrder());
        }

        public boolean putJson(final String path, final JsonObject json) {
            return this.putString(path, GsonUtil.getGson().toJson(json));
        }

        public LazyImage getShortnameImage(final String path) {
            return this.getImage(this.getFullPath(path, "png", "jpg"));
        }

        public LazyImage getImage(final String path) {
            final byte[] bytes = this.get(path);
            if (bytes == null) {
                return null;
            }

            final boolean isPng = bytes.length > 8 && bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47 && bytes[4] == (byte) 0x0D && bytes[5] == (byte) 0x0A && bytes[6] == (byte) 0x1A && bytes[7] == (byte) 0x0A;
            final boolean isJpg = bytes.length > 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF;
            if (!isPng && !isJpg) {
                return null;
            }

            return new LazyImage(bytes, isPng ? "png" : "jpg");
        }

        public boolean putPngImage(final String path, final LazyImage image) {
            return this.put(path, image.getPngBytes());
        }

        public boolean putPngImage(final String path, final BufferedImage image) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", baos);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return this.put(path, baos.toByteArray());
        }

        public void copyFrom(final Content content, final String sourcePath, final String targetPath) {
            this.put(targetPath, content.get(sourcePath));
        }

        public byte[] toZip() throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024 * 4);
            final ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
            for (final Map.Entry<String, byte[]> entry : this.content.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutputStream.write(entry.getValue());
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
            return baos.toByteArray();
        }

        public int size() {
            return this.content.size();
        }

        public static class LazyImage {

            private final byte[] bytes;
            private final String format;
            private BufferedImage image;

            public LazyImage(final byte[] bytes, final String format) {
                this.bytes = bytes;
                this.format = format;
            }

            public BufferedImage getImage() {
                if (this.image == null) {
                    try {
                        this.image = ImageIO.read(new ByteArrayInputStream(this.bytes));
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return this.image;
            }

            public byte[] getPngBytes() {
                return this.getPngBytes(false);
            }

            public byte[] getPngBytes(final boolean forceWrite) {
                if (this.format.equals("png") && !forceWrite) {
                    return this.bytes;
                } else {
                    final BufferedImage image = this.getImage();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(image, "png", baos);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    return baos.toByteArray();
                }
            }

        }

    }

}
