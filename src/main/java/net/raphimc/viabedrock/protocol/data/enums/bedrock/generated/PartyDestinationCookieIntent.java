// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PartyDestinationCookieIntent {

    Notify(0),
    OptIn(1),
    OptOut(2),
    ;

    private static final Int2ObjectMap<PartyDestinationCookieIntent> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PartyDestinationCookieIntent value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static PartyDestinationCookieIntent getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PartyDestinationCookieIntent getByValue(final int value, final PartyDestinationCookieIntent fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static PartyDestinationCookieIntent getByName(final String name) {
        for (PartyDestinationCookieIntent value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static PartyDestinationCookieIntent getByName(final String name, final PartyDestinationCookieIntent fallback) {
        for (PartyDestinationCookieIntent value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    PartyDestinationCookieIntent(final PartyDestinationCookieIntent value) {
        this(value.value);
    }

    PartyDestinationCookieIntent(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
