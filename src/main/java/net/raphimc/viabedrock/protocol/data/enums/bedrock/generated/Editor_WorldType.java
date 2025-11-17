// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum Editor_WorldType {

    NonEditor(0),
    EditorProject(1),
    EditorTestLevel(2),
    EditorRealmsUpload(3);

    private static final Int2ObjectMap<Editor_WorldType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (Editor_WorldType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static Editor_WorldType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static Editor_WorldType getByValue(final int value, final Editor_WorldType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    Editor_WorldType(final Editor_WorldType value) {
        this(value.value);
    }

    Editor_WorldType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
