// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ChatRestrictionLevel {

    None(0),
    Dropped(1),
    Disabled(2),
    ;

    private static final Int2ObjectMap<ChatRestrictionLevel> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ChatRestrictionLevel value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ChatRestrictionLevel getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ChatRestrictionLevel getByValue(final int value, final ChatRestrictionLevel fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ChatRestrictionLevel getByName(final String name) {
        for (ChatRestrictionLevel value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ChatRestrictionLevel getByName(final String name, final ChatRestrictionLevel fallback) {
        for (ChatRestrictionLevel value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ChatRestrictionLevel(final ChatRestrictionLevel value) {
        this(value.value);
    }

    ChatRestrictionLevel(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
