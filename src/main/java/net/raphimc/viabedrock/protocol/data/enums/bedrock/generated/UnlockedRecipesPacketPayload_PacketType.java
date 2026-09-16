// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum UnlockedRecipesPacketPayload_PacketType {

    Empty(0),
    InitiallyUnlockedRecipes(1),
    NewlyUnlockedRecipes(2),
    RemoveUnlockedRecipes(3),
    RemoveAllUnlockedRecipes(4),
    ;

    private static final Int2ObjectMap<UnlockedRecipesPacketPayload_PacketType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (UnlockedRecipesPacketPayload_PacketType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static UnlockedRecipesPacketPayload_PacketType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static UnlockedRecipesPacketPayload_PacketType getByValue(final int value, final UnlockedRecipesPacketPayload_PacketType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static UnlockedRecipesPacketPayload_PacketType getByName(final String name) {
        for (UnlockedRecipesPacketPayload_PacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static UnlockedRecipesPacketPayload_PacketType getByName(final String name, final UnlockedRecipesPacketPayload_PacketType fallback) {
        for (UnlockedRecipesPacketPayload_PacketType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    UnlockedRecipesPacketPayload_PacketType(final UnlockedRecipesPacketPayload_PacketType value) {
        this(value.value);
    }

    UnlockedRecipesPacketPayload_PacketType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
