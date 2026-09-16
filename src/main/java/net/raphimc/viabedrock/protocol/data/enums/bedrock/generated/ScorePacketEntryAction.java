// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ScorePacketEntryAction {

    Remove(0),
    ChangePlayer(1),
    ChangeEntity(2),
    ChangeFakePlayer(3),
    ;

    private static final Int2ObjectMap<ScorePacketEntryAction> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ScorePacketEntryAction value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ScorePacketEntryAction getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ScorePacketEntryAction getByValue(final int value, final ScorePacketEntryAction fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ScorePacketEntryAction getByName(final String name) {
        for (ScorePacketEntryAction value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ScorePacketEntryAction getByName(final String name, final ScorePacketEntryAction fallback) {
        for (ScorePacketEntryAction value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ScorePacketEntryAction(final ScorePacketEntryAction value) {
        this(value.value);
    }

    ScorePacketEntryAction(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
