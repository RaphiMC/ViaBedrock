// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ContainerID {

    container_id_none(-1),
    container_id_inventory(0),
    container_id_first(1),
    container_id_last(100),
    container_id_offhand(119),
    container_id_armor(120),
    container_id_selection_slots(122),
    container_id_player_only_ui(124),
    container_id_registry(125),
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
