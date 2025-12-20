// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MapDecoration_Type {

    MarkerWhite(0),
    MarkerGreen(1),
    MarkerRed(2),
    MarkerBlue(3),
    XWhite(4),
    TriangleRed(5),
    SquareWhite(6),
    MarkerSign(7),
    MarkerPink(8),
    MarkerOrange(9),
    MarkerYellow(10),
    MarkerTeal(11),
    TriangleGreen(12),
    SmallSquareWhite(13),
    Mansion(14),
    Monument(15),
    NoDraw(16),
    VillageDesert(17),
    VillagePlains(18),
    VillageSavanna(19),
    VillageSnowy(20),
    VillageTaiga(21),
    JungleTemple(22),
    WitchHut(23),
    TrialChambers(24),
    Player(MarkerWhite),
    PlayerOffMap(SquareWhite),
    PlayerOffLimits(SmallSquareWhite),
    PlayerHidden(NoDraw),
    ItemFrame(MarkerGreen),
    ;

    private static final Int2ObjectMap<MapDecoration_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (MapDecoration_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static MapDecoration_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static MapDecoration_Type getByValue(final int value, final MapDecoration_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static MapDecoration_Type getByName(final String name) {
        for (MapDecoration_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static MapDecoration_Type getByName(final String name, final MapDecoration_Type fallback) {
        for (MapDecoration_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    MapDecoration_Type(final MapDecoration_Type value) {
        this(value.value);
    }

    MapDecoration_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
