// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum StructureTemplateRequestOperation {

    None(0),
    ExportFromSaveMode(1),
    ExportFromLoadMode(2),
    QuerySavedStructure(3),
    ;

    private static final Int2ObjectMap<StructureTemplateRequestOperation> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (StructureTemplateRequestOperation value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static StructureTemplateRequestOperation getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static StructureTemplateRequestOperation getByValue(final int value, final StructureTemplateRequestOperation fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static StructureTemplateRequestOperation getByName(final String name) {
        for (StructureTemplateRequestOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static StructureTemplateRequestOperation getByName(final String name, final StructureTemplateRequestOperation fallback) {
        for (StructureTemplateRequestOperation value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    StructureTemplateRequestOperation(final StructureTemplateRequestOperation value) {
        this(value.value);
    }

    StructureTemplateRequestOperation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
