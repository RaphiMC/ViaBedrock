// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum HudElement {

    paperdoll(0),
    armor(1),
    tooltips(2),
    touchcontrols(3),
    crosshair(4),
    hotbar(5),
    health(6),
    progressbar(7),
    hunger(8),
    airbubbles(9),
    horsehealth(10),
    statuseffects(11),
    itemtext(12),
    ;

    private static final Int2ObjectMap<HudElement> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (HudElement value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static HudElement getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static HudElement getByValue(final int value, final HudElement fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static HudElement getByName(final String name) {
        for (HudElement value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static HudElement getByName(final String name, final HudElement fallback) {
        for (HudElement value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    HudElement(final HudElement value) {
        this(value.value);
    }

    HudElement(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
