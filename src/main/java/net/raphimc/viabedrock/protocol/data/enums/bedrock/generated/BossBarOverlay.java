// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BossBarOverlay {

    PROGRESS(0),
    NOTCHED_6(1),
    NOTCHED_10(2),
    NOTCHED_12(3),
    NOTCHED_20(4),
    ;

    private static final Int2ObjectMap<BossBarOverlay> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BossBarOverlay value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BossBarOverlay getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BossBarOverlay getByValue(final int value, final BossBarOverlay fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BossBarOverlay getByName(final String name) {
        for (BossBarOverlay value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BossBarOverlay getByName(final String name, final BossBarOverlay fallback) {
        for (BossBarOverlay value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BossBarOverlay(final BossBarOverlay value) {
        this(value.value);
    }

    BossBarOverlay(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
