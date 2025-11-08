// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LessonAction {

    Start(0),
    Complete(1),
    Restart(2);

    private static final Int2ObjectMap<LessonAction> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LessonAction value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static LessonAction getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LessonAction getByValue(final int value, final LessonAction fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    LessonAction(final LessonAction value) {
        this(value.value);
    }

    LessonAction(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
