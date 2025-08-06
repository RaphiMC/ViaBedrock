// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum CommandOriginType {

    Player(0),
    CommandBlock(1),
    MinecartCommandBlock(2),
    DevConsole(3),
    Test(4),
    AutomationPlayer(5),
    ClientAutomation(6),
    DedicatedServer(7),
    Entity(8),
    Virtual(9),
    GameArgument(10),
    EntityServer(11),
    Precompiled(12),
    GameDirectorEntityServer(13),
    Scripting(14),
    ExecuteContext(15);

    private static final Int2ObjectMap<CommandOriginType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (CommandOriginType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static CommandOriginType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static CommandOriginType getByValue(final int value, final CommandOriginType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    CommandOriginType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
