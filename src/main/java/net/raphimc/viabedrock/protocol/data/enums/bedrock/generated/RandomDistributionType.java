// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum RandomDistributionType {

    SingleValued(0),
    Uniform(1),
    Gaussian(2),
    InverseGaussian(3),
    FixedGrid(4),
    JitteredGrid(5),
    Triangle(6),
    ;

    private static final Int2ObjectMap<RandomDistributionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (RandomDistributionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static RandomDistributionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static RandomDistributionType getByValue(final int value, final RandomDistributionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static RandomDistributionType getByName(final String name) {
        for (RandomDistributionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static RandomDistributionType getByName(final String name, final RandomDistributionType fallback) {
        for (RandomDistributionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    RandomDistributionType(final RandomDistributionType value) {
        this(value.value);
    }

    RandomDistributionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
