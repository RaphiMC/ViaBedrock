// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ServerEditorConnectionPolicy {

    MatchWorldType(0),
    EditorOnly(1),
    VanillaOnly(2),
    Mixed(3),
    ;

    private static final Int2ObjectMap<ServerEditorConnectionPolicy> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ServerEditorConnectionPolicy value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ServerEditorConnectionPolicy getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ServerEditorConnectionPolicy getByValue(final int value, final ServerEditorConnectionPolicy fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ServerEditorConnectionPolicy getByName(final String name) {
        for (ServerEditorConnectionPolicy value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ServerEditorConnectionPolicy getByName(final String name, final ServerEditorConnectionPolicy fallback) {
        for (ServerEditorConnectionPolicy value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ServerEditorConnectionPolicy(final ServerEditorConnectionPolicy value) {
        this(value.value);
    }

    ServerEditorConnectionPolicy(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
