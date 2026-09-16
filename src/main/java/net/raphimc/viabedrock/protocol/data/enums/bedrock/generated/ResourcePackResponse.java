// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ResourcePackResponse {

    cancel(1),
    downloading(2),
    downloadingfinished(3),
    resourcepackstackfinished(4),
    ;

    private static final Int2ObjectMap<ResourcePackResponse> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ResourcePackResponse value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ResourcePackResponse getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ResourcePackResponse getByValue(final int value, final ResourcePackResponse fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ResourcePackResponse getByName(final String name) {
        for (ResourcePackResponse value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ResourcePackResponse getByName(final String name, final ResourcePackResponse fallback) {
        for (ResourcePackResponse value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
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
