// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BuildPlatform {

    google(1),
    ios(2),
    osx(3),
    amazon(4),
    gearvr(5),
    uwp(7),
    win32(8),
    dedicated(9),
    tvos(10),
    sony(11),
    nintendo(12),
    xbox(13),
    windowsphone(14),
    linux(15),
    unknown(-1),
    ;

    private static final Int2ObjectMap<BuildPlatform> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BuildPlatform value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BuildPlatform getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BuildPlatform getByValue(final int value, final BuildPlatform fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BuildPlatform getByName(final String name) {
        for (BuildPlatform value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BuildPlatform getByName(final String name, final BuildPlatform fallback) {
        for (BuildPlatform value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BuildPlatform(final BuildPlatform value) {
        this(value.value);
    }

    BuildPlatform(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
