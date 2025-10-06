// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ItemUseMethod {

    Unknown(-1),
    EquipArmor(0),
    Eat(1),
    Attack(2),
    Consume(3),
    Throw(4),
    Shoot(5),
    Place(6),
    FillBottle(7),
    FillBucket(8),
    PourBucket(9),
    UseTool(10),
    Interact(11),
    Retrieved(12),
    Dyed(13),
    Traded(14),
    BrushingCompleted(15),
    OpenedVault(16);

    private static final Int2ObjectMap<ItemUseMethod> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ItemUseMethod value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ItemUseMethod getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ItemUseMethod getByValue(final int value, final ItemUseMethod fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ItemUseMethod(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
