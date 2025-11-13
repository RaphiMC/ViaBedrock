// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum IdentityDefinition_Type {

    Invalid(0),
    Player(1),
    Entity(2),
    FakePlayer(3);

    private static final Int2ObjectMap<IdentityDefinition_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (IdentityDefinition_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static IdentityDefinition_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static IdentityDefinition_Type getByValue(final int value, final IdentityDefinition_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    IdentityDefinition_Type(final IdentityDefinition_Type value) {
        this(value.value);
    }

    IdentityDefinition_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
