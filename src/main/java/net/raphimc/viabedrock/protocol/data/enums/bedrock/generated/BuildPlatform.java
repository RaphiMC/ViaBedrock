// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BuildPlatform {

    Google(1),
    iOS(2),
    OSX(3),
    Amazon(4),
    UWP(7),
    Win32(8),
    Dedicated(9),
    Sony(11),
    Nx(12),
    Xbox(13),
    Linux(15),
    Unknown(-1);

    private static final Int2ObjectMap<BuildPlatform> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BuildPlatform value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static BuildPlatform getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BuildPlatform getByValue(final int value, final BuildPlatform fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    BuildPlatform(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
