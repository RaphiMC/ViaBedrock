// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum NoteBlockInstrument {

    Harp(0),
    BassDrum(1),
    Snare(2),
    Hat(3),
    Bass(4),
    Flute(5),
    Bell(6),
    Guitar(7),
    Chime(8),
    Xylophone(9),
    IronXylophone(10),
    CowBell(11),
    Didgeridoo(12),
    Bit(13),
    Banjo(14),
    Pling(15),
    Trumpet(16),
    TrumpetExposed(17),
    TrumpetWeathered(18),
    TrumpetOxidized(19),
    Zombie(20),
    Skeleton(21),
    Creeper(22),
    Dragon(23),
    WitherSkeleton(24),
    Piglin(25),
    ;

    private static final Int2ObjectMap<NoteBlockInstrument> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (NoteBlockInstrument value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static NoteBlockInstrument getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static NoteBlockInstrument getByValue(final int value, final NoteBlockInstrument fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static NoteBlockInstrument getByName(final String name) {
        for (NoteBlockInstrument value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static NoteBlockInstrument getByName(final String name, final NoteBlockInstrument fallback) {
        for (NoteBlockInstrument value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    NoteBlockInstrument(final NoteBlockInstrument value) {
        this(value.value);
    }

    NoteBlockInstrument(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
