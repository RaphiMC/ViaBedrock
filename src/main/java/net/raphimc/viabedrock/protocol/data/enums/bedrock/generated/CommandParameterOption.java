// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandParameterOption {

    None(0),
    EnumAutocompleteExpansion(0x01),
    HasSemanticConstraint(0x02),
    EnumAsChainedCommand(0x04),
    ;

    private static final Int2ObjectMap<CommandParameterOption> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandParameterOption value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CommandParameterOption getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandParameterOption getByValue(final int value, final CommandParameterOption fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CommandParameterOption getByName(final String name) {
        for (CommandParameterOption value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CommandParameterOption getByName(final String name, final CommandParameterOption fallback) {
        for (CommandParameterOption value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CommandParameterOption(final CommandParameterOption value) {
        this(value.value);
    }

    CommandParameterOption(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
