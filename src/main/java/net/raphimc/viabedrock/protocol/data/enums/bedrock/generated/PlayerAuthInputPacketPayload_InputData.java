// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayerAuthInputPacketPayload_InputData {

    ascend(0),
    descend(1),
    northjump(2),
    jumpdown(3),
    sprintdown(4),
    changeheight(5),
    jumping(6),
    autojumpinginwater(7),
    sneaking(8),
    sneakdown(9),
    up(10),
    down(11),
    left(12),
    right(13),
    upleft(14),
    upright(15),
    wantup(16),
    wantdown(17),
    wantdownslow(18),
    wantupslow(19),
    sprinting(20),
    ascendblock(21),
    descendblock(22),
    sneaktoggledown(23),
    persistsneak(24),
    startsprinting(25),
    stopsprinting(26),
    startsneaking(27),
    stopsneaking(28),
    startswimming(29),
    stopswimming(30),
    startjumping(31),
    startgliding(32),
    stopgliding(33),
    performiteminteraction(34),
    performblockactions(35),
    performitemstackrequest(36),
    handledteleport(37),
    emoting(38),
    missedswing(39),
    startcrawling(40),
    stopcrawling(41),
    startflying(42),
    stopflying(43),
    clientackserverdata(44),
    isinclientpredictedvehicle(45),
    paddlingleft(46),
    paddlingright(47),
    blockbreakingdelayenabled(48),
    horizontalcollision(49),
    verticalcollision(50),
    downleft(51),
    downright(52),
    startusingitem(53),
    iscamerarelativemovementenabled(54),
    isrotcontrolledbymovedirection(55),
    startspinattack(56),
    stopspinattack(57),
    ishotbaronlytouch(58),
    jumpreleasedraw(59),
    jumppressedraw(60),
    jumpcurrentraw(61),
    sneakreleasedraw(62),
    sneakpressedraw(63),
    sneakcurrentraw(64),
    internalupdate(65),
    ;

    private static final Int2ObjectMap<PlayerAuthInputPacketPayload_InputData> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayerAuthInputPacketPayload_InputData value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayerAuthInputPacketPayload_InputData getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayerAuthInputPacketPayload_InputData getByValue(final int value, final PlayerAuthInputPacketPayload_InputData fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayerAuthInputPacketPayload_InputData getByName(final String name) {
        for (PlayerAuthInputPacketPayload_InputData value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayerAuthInputPacketPayload_InputData getByName(final String name, final PlayerAuthInputPacketPayload_InputData fallback) {
        for (PlayerAuthInputPacketPayload_InputData value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayerAuthInputPacketPayload_InputData(final PlayerAuthInputPacketPayload_InputData value) {
        this(value.value);
    }

    PlayerAuthInputPacketPayload_InputData(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
