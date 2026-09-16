// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum LegacyTelemetryEventPacketPayload_Type {

    achievement(0),
    interaction(1),
    portalcreated(2),
    portalused(3),
    mobkilled(4),
    cauldronused(5),
    playerdied(6),
    bosskilled(7),
    agentcommand_obsolete(8),
    agentcreated(9),
    patternremoved_obsolete(10),
    slashcommand(11),
    fishbucketed_obsolete(12),
    mobborn(13),
    petdied_obsolete(14),
    poicauldronused(15),
    composterused(16),
    bellused(17),
    actordefinition(18),
    raidupdate(19),
    playermovementanomaly_obsolete(20),
    playermovementcorrected_obsolete(21),
    honeyharvested(22),
    targetblockhit(23),
    piglinbarter(24),
    playerwaxedorunwaxedcopper(25),
    codebuilderruntimeaction(26),
    codebuilderscoreboard(27),
    striderriddeninlavainoverworld(28),
    sneakclosetosculksensor(29),
    carefulrestoration(30),
    itemused(31),
    ;

    private static final Int2ObjectMap<LegacyTelemetryEventPacketPayload_Type> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (LegacyTelemetryEventPacketPayload_Type value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static LegacyTelemetryEventPacketPayload_Type getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static LegacyTelemetryEventPacketPayload_Type getByValue(final int value, final LegacyTelemetryEventPacketPayload_Type fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static LegacyTelemetryEventPacketPayload_Type getByName(final String name) {
        for (LegacyTelemetryEventPacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static LegacyTelemetryEventPacketPayload_Type getByName(final String name, final LegacyTelemetryEventPacketPayload_Type fallback) {
        for (LegacyTelemetryEventPacketPayload_Type value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    LegacyTelemetryEventPacketPayload_Type(final LegacyTelemetryEventPacketPayload_Type value) {
        this(value.value);
    }

    LegacyTelemetryEventPacketPayload_Type(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
