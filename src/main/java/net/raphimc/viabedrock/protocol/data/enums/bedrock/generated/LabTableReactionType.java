// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LabTableReactionType {

    none(0),
    icebomb(1),
    bleach(2),
    elephanttoothpaste(3),
    fertilizer(4),
    heatblock(5),
    magnesiumsalts(6),
    miscfire(7),
    miscexplosion(8),
    misclava(9),
    miscmystical(10),
    miscsmoke(11),
    misclargesmoke(12),
    ;

    private static final Int2ObjectMap<LabTableReactionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LabTableReactionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static LabTableReactionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LabTableReactionType getByValue(final int value, final LabTableReactionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static LabTableReactionType getByName(final String name) {
        for (LabTableReactionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static LabTableReactionType getByName(final String name, final LabTableReactionType fallback) {
        for (LabTableReactionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    LabTableReactionType(final LabTableReactionType value) {
        this(value.value);
    }

    LabTableReactionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
