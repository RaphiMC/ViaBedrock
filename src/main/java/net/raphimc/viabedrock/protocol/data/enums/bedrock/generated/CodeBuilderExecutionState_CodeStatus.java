// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CodeBuilderExecutionState_CodeStatus {

    none(0),
    notstarted(1),
    inprogress(2),
    paused(3),
    error(4),
    succeeded(5),
    ;

    private static final Int2ObjectMap<CodeBuilderExecutionState_CodeStatus> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CodeBuilderExecutionState_CodeStatus value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CodeBuilderExecutionState_CodeStatus getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CodeBuilderExecutionState_CodeStatus getByValue(final int value, final CodeBuilderExecutionState_CodeStatus fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CodeBuilderExecutionState_CodeStatus getByName(final String name) {
        for (CodeBuilderExecutionState_CodeStatus value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CodeBuilderExecutionState_CodeStatus getByName(final String name, final CodeBuilderExecutionState_CodeStatus fallback) {
        for (CodeBuilderExecutionState_CodeStatus value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CodeBuilderExecutionState_CodeStatus(final CodeBuilderExecutionState_CodeStatus value) {
        this(value.value);
    }

    CodeBuilderExecutionState_CodeStatus(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
