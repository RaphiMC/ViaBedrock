// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_v1_21_10_RandomDistributionType {

    SingleValued(0),
    Uniform(1),
    Gaussian(2),
    InverseGaussian(3),
    FixedGrid(4),
    JitteredGrid(5),
    Triangle(6);

    private static final Int2ObjectMap<SharedTypes_v1_21_10_RandomDistributionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_v1_21_10_RandomDistributionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static SharedTypes_v1_21_10_RandomDistributionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_v1_21_10_RandomDistributionType getByValue(final int value, final SharedTypes_v1_21_10_RandomDistributionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    SharedTypes_v1_21_10_RandomDistributionType(final SharedTypes_v1_21_10_RandomDistributionType value) {
        this(value.value);
    }

    SharedTypes_v1_21_10_RandomDistributionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
