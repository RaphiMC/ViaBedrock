// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NpcDialoguePacketPayload_NpcDialogueActionType {

    Open(0),
    Close(1);

    private static final Int2ObjectMap<NpcDialoguePacketPayload_NpcDialogueActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NpcDialoguePacketPayload_NpcDialogueActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static NpcDialoguePacketPayload_NpcDialogueActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NpcDialoguePacketPayload_NpcDialogueActionType getByValue(final int value, final NpcDialoguePacketPayload_NpcDialogueActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    NpcDialoguePacketPayload_NpcDialogueActionType(final NpcDialoguePacketPayload_NpcDialogueActionType value) {
        this(value.value);
    }

    NpcDialoguePacketPayload_NpcDialogueActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
