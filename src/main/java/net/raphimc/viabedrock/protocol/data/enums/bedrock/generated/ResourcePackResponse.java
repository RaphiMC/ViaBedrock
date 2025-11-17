// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ResourcePackResponse {

    Cancel(1),
    Downloading(2),
    DownloadingFinished(3),
    ResourcePackStackFinished(4);

    private static final Int2ObjectMap<ResourcePackResponse> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ResourcePackResponse value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ResourcePackResponse getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ResourcePackResponse getByValue(final int value, final ResourcePackResponse fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ResourcePackResponse(final ResourcePackResponse value) {
        this(value.value);
    }

    ResourcePackResponse(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
