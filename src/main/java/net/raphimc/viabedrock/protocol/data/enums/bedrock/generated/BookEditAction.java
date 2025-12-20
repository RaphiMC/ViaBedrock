// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum BookEditAction {

    ReplacePage(0),
    AddPage(1),
    DeletePage(2),
    SwapPages(3),
    Finalize(4),
    ;

    private static final Int2ObjectMap<BookEditAction> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (BookEditAction value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static BookEditAction getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static BookEditAction getByValue(final int value, final BookEditAction fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static BookEditAction getByName(final String name) {
        for (BookEditAction value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static BookEditAction getByName(final String name, final BookEditAction fallback) {
        for (BookEditAction value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    BookEditAction(final BookEditAction value) {
        this(value.value);
    }

    BookEditAction(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
