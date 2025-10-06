// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AgentActionType {

    Attack(1),
    Collect(2),
    Destroy(3),
    DetectRedstone(4),
    DetectObstacle(5),
    Drop(6),
    DropAll(7),
    Inspect(8),
    InspectData(9),
    InspectItemCount(10),
    InspectItemDetail(11),
    InspectItemSpace(12),
    Interact(13),
    Move(14),
    PlaceBlock(15),
    Till(16),
    TransferItemTo(17),
    Turn(18);

    private static final Int2ObjectMap<AgentActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AgentActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static AgentActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AgentActionType getByValue(final int value, final AgentActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    AgentActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
