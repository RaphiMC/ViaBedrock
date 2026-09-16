// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NpcDialogueActionType {

    Open(0),
    Close(1),
    ;

    private static final Int2ObjectMap<NpcDialogueActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NpcDialogueActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static NpcDialogueActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NpcDialogueActionType getByValue(final int value, final NpcDialogueActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static NpcDialogueActionType getByName(final String name) {
        for (NpcDialogueActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static NpcDialogueActionType getByName(final String name, final NpcDialogueActionType fallback) {
        for (NpcDialogueActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    NpcDialogueActionType(final NpcDialogueActionType value) {
        this(value.value);
    }

    NpcDialogueActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
