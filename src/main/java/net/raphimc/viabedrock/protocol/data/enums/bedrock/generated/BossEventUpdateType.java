// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BossEventUpdateType {

    Add(0),
    PlayerAdded(1),
    Remove(2),
    PlayerRemoved(3),
    Update_Percent(4),
    Update_Name(5),
    Update_Properties(6),
    Update_Style(7),
    Query(8);

    private static final Int2ObjectMap<BossEventUpdateType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BossEventUpdateType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static BossEventUpdateType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BossEventUpdateType getByValue(final int value, final BossEventUpdateType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
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
