// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum SharedTypes_Legacy_ActorDamageCause {

    override(0),
    contact(1),
    entityattack(2),
    projectile(3),
    suffocation(4),
    fall(5),
    fire(6),
    firetick(7),
    lava(8),
    drowning(9),
    blockexplosion(10),
    entityexplosion(11),
    void(12),
    selfdestruct(13),
    magic(14),
    wither(15),
    starve(16),
    anvil(17),
    thorns(18),
    fallingblock(19),
    piston(20),
    flyintowall(21),
    magma(22),
    fireworks(23),
    lightning(24),
    charging(25),
    temperature(26),
    freezing(27),
    stalactite(28),
    stalagmite(29),
    ramattack(30),
    sonicboom(31),
    campfire(32),
    soulcampfire(33),
    macesmash(34),
    ;

    private static final Int2ObjectMap<SharedTypes_Legacy_ActorDamageCause> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (SharedTypes_Legacy_ActorDamageCause value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static SharedTypes_Legacy_ActorDamageCause getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static SharedTypes_Legacy_ActorDamageCause getByValue(final int value, final SharedTypes_Legacy_ActorDamageCause fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static SharedTypes_Legacy_ActorDamageCause getByName(final String name) {
        for (SharedTypes_Legacy_ActorDamageCause value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static SharedTypes_Legacy_ActorDamageCause getByName(final String name, final SharedTypes_Legacy_ActorDamageCause fallback) {
        for (SharedTypes_Legacy_ActorDamageCause value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    SharedTypes_Legacy_ActorDamageCause(final SharedTypes_Legacy_ActorDamageCause value) {
        this(value.value);
    }

    SharedTypes_Legacy_ActorDamageCause(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
