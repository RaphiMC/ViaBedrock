// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum AnimatePacket_Action {

    /**
     * Unused
     */
    NoAction(0),
    /**
     * Server bound notification to swing the player's arm. Server is expected to rebroadcast to all that should see the arm move<br>
     * See also PlayerAuthInputPacket::InputData::MissedSwing for a very similar action
     */
    Swing(1),
    /**
     * Client bound notification to stop sleeping in a bed
     */
    WakeUp(3),
    /**
     * Client-bound notification to play critical hit particles
     */
    CriticalHit(4),
    /**
     * Unused
     */
    MagicCriticalHit(5),
    ;

    private static final Int2ObjectMap<AnimatePacket_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (AnimatePacket_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static AnimatePacket_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static AnimatePacket_Action getByValue(final int value, final AnimatePacket_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static AnimatePacket_Action getByName(final String name) {
        for (AnimatePacket_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static AnimatePacket_Action getByName(final String name, final AnimatePacket_Action fallback) {
        for (AnimatePacket_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    AnimatePacket_Action(final AnimatePacket_Action value) {
        this(value.value);
    }

    AnimatePacket_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
