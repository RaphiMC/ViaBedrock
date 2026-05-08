// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ScriptModuleMinecraft_ScriptPrimitiveShapeType {

    Line(0),
    Box(1),
    Sphere(2),
    Circle(3),
    Text(4),
    Arrow(5),
    ;

    private static final Int2ObjectMap<ScriptModuleMinecraft_ScriptPrimitiveShapeType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ScriptModuleMinecraft_ScriptPrimitiveShapeType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ScriptModuleMinecraft_ScriptPrimitiveShapeType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ScriptModuleMinecraft_ScriptPrimitiveShapeType getByValue(final int value, final ScriptModuleMinecraft_ScriptPrimitiveShapeType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ScriptModuleMinecraft_ScriptPrimitiveShapeType getByName(final String name) {
        for (ScriptModuleMinecraft_ScriptPrimitiveShapeType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ScriptModuleMinecraft_ScriptPrimitiveShapeType getByName(final String name, final ScriptModuleMinecraft_ScriptPrimitiveShapeType fallback) {
        for (ScriptModuleMinecraft_ScriptPrimitiveShapeType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ScriptModuleMinecraft_ScriptPrimitiveShapeType(final ScriptModuleMinecraft_ScriptPrimitiveShapeType value) {
        this(value.value);
    }

    ScriptModuleMinecraft_ScriptPrimitiveShapeType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
