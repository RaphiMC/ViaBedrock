// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum DataDrivenScreenClosedReason {

    ProgrammaticClose(0),
    ProgrammaticCloseAll(1),
    ClientCanceled(2),
    UserBusy(3),
    InvalidForm(4),
    ;

    private static final Int2ObjectMap<DataDrivenScreenClosedReason> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (DataDrivenScreenClosedReason value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static DataDrivenScreenClosedReason getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static DataDrivenScreenClosedReason getByValue(final int value, final DataDrivenScreenClosedReason fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static DataDrivenScreenClosedReason getByName(final String name) {
        for (DataDrivenScreenClosedReason value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static DataDrivenScreenClosedReason getByName(final String name, final DataDrivenScreenClosedReason fallback) {
        for (DataDrivenScreenClosedReason value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    DataDrivenScreenClosedReason(final DataDrivenScreenClosedReason value) {
        this(value.value);
    }

    DataDrivenScreenClosedReason(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
