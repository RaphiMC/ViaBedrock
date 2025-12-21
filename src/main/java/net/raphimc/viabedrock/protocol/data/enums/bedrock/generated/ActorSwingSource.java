// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorSwingSource {

    None(0),
    Build(1),
    Mine(2),
    Interact(3),
    Attack(4),
    UseItem(5),
    ThrowItem(6),
    DropItem(7),
    Event(8),
    ;

    private static final Int2ObjectMap<ActorSwingSource> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorSwingSource value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ActorSwingSource getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorSwingSource getByValue(final int value, final ActorSwingSource fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ActorSwingSource getByName(final String name) {
        for (ActorSwingSource value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ActorSwingSource getByName(final String name, final ActorSwingSource fallback) {
        for (ActorSwingSource value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ActorSwingSource(final ActorSwingSource value) {
        this(value.value);
    }

    ActorSwingSource(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
