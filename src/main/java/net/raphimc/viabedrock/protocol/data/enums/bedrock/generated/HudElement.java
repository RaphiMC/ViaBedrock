// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum HudElement {

    PaperDoll(0),
    Armor(1),
    ToolTips(2),
    TouchControls(3),
    Crosshair(4),
    HotBar(5),
    Health(6),
    ProgressBar(7),
    Hunger(8),
    AirBubbles(9),
    HorseHealth(10),
    StatusEffects(11),
    ItemText(12);

    private static final Int2ObjectMap<HudElement> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (HudElement value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static HudElement getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static HudElement getByValue(final int value, final HudElement fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    HudElement(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
