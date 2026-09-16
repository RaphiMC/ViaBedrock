// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerActionType {

    unknown(-1),
    startdestroyblock(0),
    abortdestroyblock(1),
    stopdestroyblock(2),
    getupdatedblock(3),
    dropitem(4),
    startsleeping(5),
    stopsleeping(6),
    respawn(7),
    startjump(8),
    startsprinting(9),
    stopsprinting(10),
    startsneaking(11),
    stopsneaking(12),
    creativedestroyblock(13),
    changedimensionack(14),
    startgliding(15),
    stopgliding(16),
    denydestroyblock(17),
    crackblock(18),
    changeskin(19),
    updatedenchantingseed(20),
    startswimming(21),
    stopswimming(22),
    startspinattack(23),
    stopspinattack(24),
    interactwithblock(25),
    predictdestroyblock(26),
    continuedestroyblock(27),
    startitemuseon(28),
    stopitemuseon(29),
    handledteleport(30),
    missedswing(31),
    startcrawling(32),
    stopcrawling(33),
    startflying(34),
    stopflying(35),
    clientackserverdata(36),
    startusingitem(37),
    internalupdate(38),
    count(39),
    ;

    private static final Int2ObjectMap<PlayerActionType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerActionType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerActionType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerActionType getByValue(final int value, final PlayerActionType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerActionType getByName(final String name) {
        for (PlayerActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerActionType getByName(final String name, final PlayerActionType fallback) {
        for (PlayerActionType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerActionType(final PlayerActionType value) {
        this(value.value);
    }

    PlayerActionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
