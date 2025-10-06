// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ModalFormCancelReason {

    UserClosed(0),
    UserBusy(1);

    private static final Int2ObjectMap<ModalFormCancelReason> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ModalFormCancelReason value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ModalFormCancelReason getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ModalFormCancelReason getByValue(final int value, final ModalFormCancelReason fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ModalFormCancelReason(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
