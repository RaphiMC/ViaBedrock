// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ScriptModuleDebugUtilities_ScriptDebugShapeType {

    Line(0),
    Box(1),
    Sphere(2),
    Circle(3),
    Text(4),
    Arrow(5),
    NumShapeTypes(6);

    private static final Int2ObjectMap<ScriptModuleDebugUtilities_ScriptDebugShapeType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ScriptModuleDebugUtilities_ScriptDebugShapeType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ScriptModuleDebugUtilities_ScriptDebugShapeType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ScriptModuleDebugUtilities_ScriptDebugShapeType getByValue(final int value, final ScriptModuleDebugUtilities_ScriptDebugShapeType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ScriptModuleDebugUtilities_ScriptDebugShapeType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
