// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Enchant_Type {

    Protection(0),
    FireProtection(1),
    FeatherFalling(2),
    BlastProtection(3),
    ProjectileProtection(4),
    Thorns(5),
    Respiration(6),
    DepthStrider(7),
    AquaAffinity(8),
    Sharpness(9),
    Smite(10),
    BaneOfArthropods(11),
    Knockback(12),
    FireAspect(13),
    Looting(14),
    Efficiency(15),
    SilkTouch(16),
    Unbreaking(17),
    Fortune(18),
    Power(19),
    Punch(20),
    Flame(21),
    Infinity(22),
    LuckOfTheSea(23),
    Lure(24),
    FrostWalker(25),
    Mending(26),
    CurseOfBinding(27),
    CurseOfVanishing(28),
    Impaling(29),
    Riptide(30),
    Loyalty(31),
    Channeling(32),
    Multishot(33),
    Piercing(34),
    QuickCharge(35),
    SoulSpeed(36),
    SwiftSneak(37),
    WindBurst(38),
    Density(39),
    Breach(40),
    InvalidEnchantment(42),
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
