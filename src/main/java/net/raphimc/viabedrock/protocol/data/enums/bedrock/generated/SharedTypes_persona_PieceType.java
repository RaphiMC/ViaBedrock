// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_persona_PieceType {

    unknown(0),
    skeleton(1),
    body(2),
    skin(3),
    bottom(4),
    feet(5),
    dress(6),
    top(7),
    high_pants(8),
    hands(9),
    outerwear(10),
    facialhair(11),
    mouth(12),
    eyes(13),
    hair(14),
    hood(15),
    back(16),
    faceaccessory(17),
    head(18),
    legs(19),
    leftleg(20),
    rightleg(21),
    arms(22),
    leftarm(23),
    rightarm(24),
    capes(25),
    classicskin(26),
    emote(27),
    unsupported(28),
    ;

    private static final Int2ObjectMap<SharedTypes_persona_PieceType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_persona_PieceType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_persona_PieceType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_persona_PieceType getByValue(final int value, final SharedTypes_persona_PieceType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_persona_PieceType getByName(final String name) {
        for (SharedTypes_persona_PieceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_persona_PieceType getByName(final String name, final SharedTypes_persona_PieceType fallback) {
        for (SharedTypes_persona_PieceType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_persona_PieceType(final SharedTypes_persona_PieceType value) {
        this(value.value);
    }

    SharedTypes_persona_PieceType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
