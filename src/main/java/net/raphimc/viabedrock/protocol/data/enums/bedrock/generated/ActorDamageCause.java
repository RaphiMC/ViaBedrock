// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ActorDamageCause {

    None(-1),
    Override(0),
    Contact(1),
    EntityAttack(2),
    Projectile(3),
    Suffocation(4),
    Fall(5),
    Fire(6),
    FireTick(7),
    Lava(8),
    Drowning(9),
    BlockExplosion(10),
    EntityExplosion(11),
    Void(12),
    SelfDestruct(13),
    Magic(14),
    Wither(15),
    Starve(16),
    Anvil(17),
    Thorns(18),
    FallingBlock(19),
    Piston(20),
    FlyIntoWall(21),
    Magma(22),
    Fireworks(23),
    Lightning(24),
    Charging(25),
    Temperature(26),
    Freezing(27),
    Stalactite(28),
    Stalagmite(29),
    RamAttack(30),
    SonicBoom(31),
    Campfire(32),
    SoulCampfire(33),
    MaceSmash(34);

    private static final Int2ObjectMap<ActorDamageCause> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ActorDamageCause value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ActorDamageCause getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ActorDamageCause getByValue(final int value, final ActorDamageCause fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ActorDamageCause(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
