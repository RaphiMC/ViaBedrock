// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Enchant_Type {

    protection(0),
    fireprotection(1),
    featherfalling(2),
    blastprotection(3),
    projectileprotection(4),
    thorns(5),
    respiration(6),
    depthstrider(7),
    aquaaffinity(8),
    sharpness(9),
    smite(10),
    baneofarthropods(11),
    knockback(12),
    fireaspect(13),
    looting(14),
    efficiency(15),
    silktouch(16),
    unbreaking(17),
    fortune(18),
    power(19),
    punch(20),
    flame(21),
    infinity(22),
    luckofthesea(23),
    lure(24),
    frostwalker(25),
    mending(26),
    curseofbinding(27),
    curseofvanishing(28),
    impaling(29),
    riptide(30),
    loyalty(31),
    channeling(32),
    multishot(33),
    piercing(34),
    quickcharge(35),
    soulspeed(36),
    swiftsneak(37),
    windburst(38),
    density(39),
    breach(40),
    lunge(41),
    numenchantments(42),
    invalidenchantment(43),
    ;

    private static final Int2ObjectMap<Enchant_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Enchant_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Enchant_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Enchant_Type getByValue(final int value, final Enchant_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Enchant_Type getByName(final String name) {
        for (Enchant_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Enchant_Type getByName(final String name, final Enchant_Type fallback) {
        for (Enchant_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Enchant_Type(final Enchant_Type value) {
        this(value.value);
    }

    Enchant_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
