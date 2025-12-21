// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ServerAuthMovementMode {

    LegacyClientAuthoritativeV1_Deprecated(0),
    /**
     * Referred to in the rest of the documentation as 'Client Authoritative'.<br>
     * This mode is the current default with previews coming soon for migrating to server authoritative<br>
     * The packets sent from client to server are largely the same as for server authoritative, see their documentation for details:<br>
     * - PlayerAuthInputPacket (Primary motion and input data)<br>
     * - InventoryTransactionPacket (Item use, tangentially movement related)<br>
     * PlayerActionPacket is sent in some cases, and in others has become a bit inside of PlayerAuthInputPacket.<br>
     * The client can be repositioned with:<br>
     * - MovePlayerPacket<br>
     * - SetActorMotionPacket
     */
    ClientAuthoritativeV2(1),
    /**
     * Referred to in the rest of the documentation as 'Server Authoritative'<br>
     * This mode is intended to become the new default after previews coming soon.<br>
     * The packets from client to server are similar.<br>
     * - PlayerAuthInputPacket (Primary motion and input data)<br>
     * - InventoryTransactionPacket (Item use, tangentially movement related)<br>
     * PlayerActionPacket is sent in some cases, and in others has become a bit inside of PlayerAuthInputPacket.<br>
     * The client can be repositioned with:<br>
     * - MovePlayerPacket<br>
     * - CorrectPlayerMovePredictionPacket<br>
     * - SetActorMotionPacket<br>
     * Additionally, in this mode many client-bound packets have a 'Tick' value. These echo back the tick value that the client supplies in the PlayerAuthInputPacket.<br>
     * For packets relating to a player or client predicted vehicle, the tick value should be that of the most recently processed PlayerAuthInputPacket from the player.<br>
     * Specifying zero is also acceptable although may result in minor visual flickering as it may confuse client predicted actions.
     */
    ServerAuthoritativeV3(2),
    ;

    private static final Int2ObjectMap<ServerAuthMovementMode> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ServerAuthMovementMode value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static ServerAuthMovementMode getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ServerAuthMovementMode getByValue(final int value, final ServerAuthMovementMode fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static ServerAuthMovementMode getByName(final String name) {
        for (ServerAuthMovementMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static ServerAuthMovementMode getByName(final String name, final ServerAuthMovementMode fallback) {
        for (ServerAuthMovementMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    ServerAuthMovementMode(final ServerAuthMovementMode value) {
        this(value.value);
    }

    ServerAuthMovementMode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
