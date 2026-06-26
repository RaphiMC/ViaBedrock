// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LegacyTelemetryEventPacket_Type {

    Achievement(0),
    Interaction(1),
    PortalCreated(2),
    PortalUsed(3),
    MobKilled(4),
    CauldronUsed(5),
    PlayerDied(6),
    BossKilled(7),
    AgentCommand_OBSOLETE(8),
    AgentCreated(9),
    PatternRemoved_OBSOLETE(10),
    SlashCommand(11),
    Deprecated_FishBucketed(12),
    MobBorn(13),
    PetDied_OBSOLETE(14),
    POICauldronUsed(15),
    ComposterUsed(16),
    BellUsed(17),
    ActorDefinition(18),
    RaidUpdate(19),
    PlayerMovementAnomaly_OBSOLETE(20),
    PlayerMovementCorrected_OBSOLETE(21),
    HoneyHarvested(22),
    TargetBlockHit(23),
    PiglinBarter(24),
    PlayerWaxedOrUnwaxedCopper(25),
    CodeBuilderRuntimeAction(26),
    CodeBuilderScoreboard(27),
    StriderRiddenInLavaInOverworld(28),
    SneakCloseToSculkSensor(29),
    CarefulRestoration(30),
    ItemUsedEvent(31),
    ;

    private static final Int2ObjectMap<LegacyTelemetryEventPacket_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LegacyTelemetryEventPacket_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static LegacyTelemetryEventPacket_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LegacyTelemetryEventPacket_Type getByValue(final int value, final LegacyTelemetryEventPacket_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static LegacyTelemetryEventPacket_Type getByName(final String name) {
        for (LegacyTelemetryEventPacket_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static LegacyTelemetryEventPacket_Type getByName(final String name, final LegacyTelemetryEventPacket_Type fallback) {
        for (LegacyTelemetryEventPacket_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    LegacyTelemetryEventPacket_Type(final LegacyTelemetryEventPacket_Type value) {
        this(value.value);
    }

    LegacyTelemetryEventPacket_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
