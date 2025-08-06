// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum PlayStatus {

    LoginSuccess(0),
    LoginFailed_ClientOld(1),
    LoginFailed_ServerOld(2),
    PlayerSpawn(3),
    LoginFailed_InvalidTenant(4),
    LoginFailed_EditionMismatchEduToVanilla(5),
    LoginFailed_EditionMismatchVanillaToEdu(6),
    LoginFailed_ServerFullSubClient(7),
    LoginFailed_EditorMismatchEditorToVanilla(8),
    LoginFailed_EditorMismatchVanillaToEditor(9);

    private static final Int2ObjectMap<PlayStatus> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (PlayStatus value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static PlayStatus getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static PlayStatus getByValue(final int value, final PlayStatus fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    PlayStatus(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
