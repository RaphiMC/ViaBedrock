// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MapItemTrackedActor_Type {

    Entity(0),
    BlockEntity(1),
    Other(2);

    private static final Int2ObjectMap<MapItemTrackedActor_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MapItemTrackedActor_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static MapItemTrackedActor_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MapItemTrackedActor_Type getByValue(final int value, final MapItemTrackedActor_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    MapItemTrackedActor_Type(final MapItemTrackedActor_Type value) {
        this(value.value);
    }

    MapItemTrackedActor_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
