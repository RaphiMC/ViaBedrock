// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Lesson_Action {

    Start(0),
    Complete(1),
    Restart(2),
    ;

    private static final Int2ObjectMap<Lesson_Action> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Lesson_Action value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static Lesson_Action getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Lesson_Action getByValue(final int value, final Lesson_Action fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static Lesson_Action getByName(final String name) {
        for (Lesson_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static Lesson_Action getByName(final String name, final Lesson_Action fallback) {
        for (Lesson_Action value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    Lesson_Action(final Lesson_Action value) {
        this(value.value);
    }

    Lesson_Action(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
