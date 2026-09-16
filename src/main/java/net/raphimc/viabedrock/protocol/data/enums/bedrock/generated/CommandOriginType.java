// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandOriginType {

    player(0),
    commandblock(1),
    minecartcommandblock(2),
    devconsole(3),
    test(4),
    automationplayer(5),
    clientautomation(6),
    dedicatedserver(7),
    entity(8),
    virtual(9),
    gameargument(10),
    entityserver(11),
    precompiled(12),
    gamedirectorentityserver(13),
    scripting(14),
    executecontext(15),
    ;

    private static final Int2ObjectMap<CommandOriginType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandOriginType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static CommandOriginType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandOriginType getByValue(final int value, final CommandOriginType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static CommandOriginType getByName(final String name) {
        for (CommandOriginType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static CommandOriginType getByName(final String name, final CommandOriginType fallback) {
        for (CommandOriginType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    CommandOriginType(final CommandOriginType value) {
        this(value.value);
    }

    CommandOriginType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
