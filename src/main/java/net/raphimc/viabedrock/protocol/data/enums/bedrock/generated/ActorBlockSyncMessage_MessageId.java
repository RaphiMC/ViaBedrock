// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorBlockSyncMessage_MessageId {

    NONE(0),
    CREATE(1),
    DESTROY(2),
    ;

    private static final Int2ObjectMap<ActorBlockSyncMessage_MessageId> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorBlockSyncMessage_MessageId value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ActorBlockSyncMessage_MessageId getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorBlockSyncMessage_MessageId getByValue(final int value, final ActorBlockSyncMessage_MessageId fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ActorBlockSyncMessage_MessageId getByName(final String name) {
        for (ActorBlockSyncMessage_MessageId value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ActorBlockSyncMessage_MessageId getByName(final String name, final ActorBlockSyncMessage_MessageId fallback) {
        for (ActorBlockSyncMessage_MessageId value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ActorBlockSyncMessage_MessageId(final ActorBlockSyncMessage_MessageId value) {
        this(value.value);
    }

    ActorBlockSyncMessage_MessageId(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
