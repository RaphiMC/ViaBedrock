// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum InputMode {

    Undefined(0),
    Mouse(1),
    Touch(2),
    GamePad(3),
    MotionController(4);

    private static final Int2ObjectMap<InputMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (InputMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static InputMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static InputMode getByValue(final int value, final InputMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    InputMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
