// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum EditorWorldType {

    NonEditor(0),
    EditorProject(1),
    EditorTestLevel(2),
    EditorRealmsUpload(3),
    ;

    private static final Int2ObjectMap<EditorWorldType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (EditorWorldType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static EditorWorldType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static EditorWorldType getByValue(final int value, final EditorWorldType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static EditorWorldType getByName(final String name) {
        for (EditorWorldType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static EditorWorldType getByName(final String name, final EditorWorldType fallback) {
        for (EditorWorldType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    EditorWorldType(final EditorWorldType value) {
        this(value.value);
    }

    EditorWorldType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
