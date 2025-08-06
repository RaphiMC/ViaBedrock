// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandOutputType {

    None(0),
    LastOutput(1),
    Silent(2),
    AllOutput(3),
    DataSet(4);

    private static final Int2ObjectMap<CommandOutputType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandOutputType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CommandOutputType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandOutputType getByValue(final int value, final CommandOutputType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CommandOutputType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
