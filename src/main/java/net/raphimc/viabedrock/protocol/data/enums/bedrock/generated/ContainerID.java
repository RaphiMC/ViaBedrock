// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ContainerID {

    CONTAINER_ID_NONE(-1),
    CONTAINER_ID_INVENTORY(0),
    CONTAINER_ID_FIRST(1),
    CONTAINER_ID_LAST(100),
    CONTAINER_ID_OFFHAND(119),
    CONTAINER_ID_ARMOR(120),
    CONTAINER_ID_SELECTION_SLOTS(122),
    CONTAINER_ID_PLAYER_ONLY_UI(124),
    CONTAINER_ID_REGISTRY(125),
    ;

    private static final Int2ObjectMap<ContainerID> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ContainerID value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ContainerID getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ContainerID getByValue(final int value, final ContainerID fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ContainerID getByName(final String name) {
        for (ContainerID value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ContainerID getByName(final String name, final ContainerID fallback) {
        for (ContainerID value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ContainerID(final ContainerID value) {
        this(value.value);
    }

    ContainerID(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
