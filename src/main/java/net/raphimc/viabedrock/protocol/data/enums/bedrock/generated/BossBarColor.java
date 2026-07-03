// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BossBarColor {

    PINK(0),
    BLUE(1),
    RED(2),
    GREEN(3),
    YELLOW(4),
    PURPLE(5),
    REBECCA_PURPLE(6),
    WHITE(7),
    ;

    private static final Int2ObjectMap<BossBarColor> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BossBarColor value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BossBarColor getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BossBarColor getByValue(final int value, final BossBarColor fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BossBarColor getByName(final String name) {
        for (BossBarColor value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BossBarColor getByName(final String name, final BossBarColor fallback) {
        for (BossBarColor value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BossBarColor(final BossBarColor value) {
        this(value.value);
    }

    BossBarColor(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
