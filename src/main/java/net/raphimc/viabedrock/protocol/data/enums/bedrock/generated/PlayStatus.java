// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayStatus {

    loginsuccess(0),
    loginfailed_clientold(1),
    loginfailed_serverold(2),
    playerspawn(3),
    loginfailed_invalidtenant(4),
    loginfailed_editionmismatchedutovanilla(5),
    loginfailed_editionmismatchvanillatoedu(6),
    loginfailed_serverfullsubclient(7),
    loginfailed_editormismatcheditortovanilla(8),
    loginfailed_editormismatchvanillatoeditor(9),
    ;

    private static final Int2ObjectMap<PlayStatus> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayStatus value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PlayStatus getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayStatus getByValue(final int value, final PlayStatus fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PlayStatus getByName(final String name) {
        for (PlayStatus value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PlayStatus getByName(final String name, final PlayStatus fallback) {
        for (PlayStatus value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PlayStatus(final PlayStatus value) {
        this(value.value);
    }

    PlayStatus(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
