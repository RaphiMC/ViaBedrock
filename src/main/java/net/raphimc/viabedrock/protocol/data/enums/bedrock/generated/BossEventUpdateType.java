// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BossEventUpdateType {

    add(0),
    playeradded(1),
    remove(2),
    playerremoved(3),
    update_percent(4),
    update_name(5),
    update_properties(6),
    update_style(7),
    query(8),
    ;

    private static final Int2ObjectMap<BossEventUpdateType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BossEventUpdateType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BossEventUpdateType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BossEventUpdateType getByValue(final int value, final BossEventUpdateType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BossEventUpdateType getByName(final String name) {
        for (BossEventUpdateType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BossEventUpdateType getByName(final String name, final BossEventUpdateType fallback) {
        for (BossEventUpdateType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BossEventUpdateType(final BossEventUpdateType value) {
        this(value.value);
    }

    BossEventUpdateType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
