// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandBlockMode {

    Normal(0),
    Repeating(1),
    Chain(2);

    private static final Int2ObjectMap<CommandBlockMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandBlockMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CommandBlockMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandBlockMode getByValue(final int value, final CommandBlockMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CommandBlockMode(final CommandBlockMode value) {
        this(value.value);
    }

    CommandBlockMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
