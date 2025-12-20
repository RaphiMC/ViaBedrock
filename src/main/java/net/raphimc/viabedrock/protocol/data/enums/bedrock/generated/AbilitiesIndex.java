// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AbilitiesIndex {

    Invalid(-1),
    Build(0),
    Mine(1),
    DoorsAndSwitches(2),
    OpenContainers(3),
    AttackPlayers(4),
    AttackMobs(5),
    OperatorCommands(6),
    Teleport(7),
    Invulnerable(8),
    Flying(9),
    MayFly(10),
    Instabuild(11),
    Lightning(12),
    FlySpeed(13),
    WalkSpeed(14),
    Muted(15),
    WorldBuilder(16),
    NoClip(17),
    PrivilegedBuilder(18),
    VerticalFlySpeed(19),
    ;

    private static final Int2ObjectMap<AbilitiesIndex> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AbilitiesIndex value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static AbilitiesIndex getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AbilitiesIndex getByValue(final int value, final AbilitiesIndex fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static AbilitiesIndex getByName(final String name) {
        for (AbilitiesIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static AbilitiesIndex getByName(final String name, final AbilitiesIndex fallback) {
        for (AbilitiesIndex value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    AbilitiesIndex(final AbilitiesIndex value) {
        this(value.value);
    }

    AbilitiesIndex(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
