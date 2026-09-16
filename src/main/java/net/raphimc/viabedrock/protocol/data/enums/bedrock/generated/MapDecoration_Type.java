// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum MapDecoration_Type {

    markerwhite(0),
    markergreen(1),
    markerred(2),
    markerblue(3),
    xwhite(4),
    trianglered(5),
    squarewhite(6),
    markersign(7),
    markerpink(8),
    markerorange(9),
    markeryellow(10),
    markerteal(11),
    trianglegreen(12),
    smallsquarewhite(13),
    mansion(14),
    monument(15),
    nodraw(16),
    villagedesert(17),
    villageplains(18),
    villagesavanna(19),
    villagesnowy(20),
    villagetaiga(21),
    jungletemple(22),
    witchhut(23),
    trialchambers(24),
    abandonedcamp(25),
    buriedancientcity(26),
    buriedmineshaft(27),
    desertpyramid(28),
    warmoceanruins(29),
    count(30),
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
