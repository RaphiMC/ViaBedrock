// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum persona_PieceType {

    Unknown(0),
    Skeleton(1),
    Body(2),
    Skin(3),
    Bottom(4),
    Feet(5),
    Dress(6),
    Top(7),
    High_Pants(8),
    Hands(9),
    Outerwear(10),
    FacialHair(11),
    Mouth(12),
    Eyes(13),
    Hair(14),
    Hood(15),
    Back(16),
    FaceAccessory(17),
    Head(18),
    Legs(19),
    LeftLeg(20),
    RightLeg(21),
    Arms(22),
    LeftArm(23),
    RightArm(24),
    Capes(25),
    ClassicSkin(26),
    Emote(27),
    CoCo(28),
    Unsupported(29),
    ;

    private static final Int2ObjectMap<persona_PieceType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (persona_PieceType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static persona_PieceType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static persona_PieceType getByValue(final int value, final persona_PieceType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static persona_PieceType getByName(final String name) {
        for (persona_PieceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static persona_PieceType getByName(final String name, final persona_PieceType fallback) {
        for (persona_PieceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    persona_PieceType(final persona_PieceType value) {
        this(value.value);
    }

    persona_PieceType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
