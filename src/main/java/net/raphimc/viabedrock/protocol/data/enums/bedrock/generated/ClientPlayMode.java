// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ClientPlayMode {

    Normal(0),
    Teaser(1),
    Screen(2),
    Viewer(3),
    Reality(4),
    Placement(5),
    LivingRoom(6),
    ExitLevel(7),
    ExitLevelLivingRoom(8);

    private static final Int2ObjectMap<ClientPlayMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ClientPlayMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ClientPlayMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ClientPlayMode getByValue(final int value, final ClientPlayMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ClientPlayMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
