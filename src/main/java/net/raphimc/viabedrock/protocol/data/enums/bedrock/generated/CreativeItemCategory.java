// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CreativeItemCategory {

    Construction(1),
    Nature(2),
    Equipment(3),
    Items(4),
    ItemCommandOnly(5),
    Undefined(6);

    private static final Int2ObjectMap<CreativeItemCategory> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CreativeItemCategory value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CreativeItemCategory getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CreativeItemCategory getByValue(final int value, final CreativeItemCategory fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CreativeItemCategory(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
