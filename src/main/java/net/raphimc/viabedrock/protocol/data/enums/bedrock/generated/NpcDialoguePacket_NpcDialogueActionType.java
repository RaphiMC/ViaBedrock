// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NpcDialoguePacket_NpcDialogueActionType {

    Open(0),
    Close(1);

    private static final Int2ObjectMap<NpcDialoguePacket_NpcDialogueActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NpcDialoguePacket_NpcDialogueActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static NpcDialoguePacket_NpcDialogueActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NpcDialoguePacket_NpcDialogueActionType getByValue(final int value, final NpcDialoguePacket_NpcDialogueActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    NpcDialoguePacket_NpcDialogueActionType(final NpcDialoguePacket_NpcDialogueActionType value) {
        this(value.value);
    }

    NpcDialoguePacket_NpcDialogueActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
