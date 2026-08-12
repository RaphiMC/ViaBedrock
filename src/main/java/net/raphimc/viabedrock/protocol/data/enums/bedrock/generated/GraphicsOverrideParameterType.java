// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GraphicsOverrideParameterType {

    SkyZenithColor(0),
    SkyHorizonColor(1),
    HorizonBlendMin(2),
    HorizonBlendMax(3),
    HorizonBlendStart(4),
    HorizonBlendMieStart(5),
    RayleighStrength(6),
    SunMieStrength(7),
    MoonMieStrength(8),
    SunGlareShape(9),
    Chlorophyll(10),
    CDOM(11),
    SuspendedSediment(12),
    WavesDepth(13),
    WavesFrequency(14),
    WavesFrequencyScaling(15),
    WavesSpeed(16),
    WavesSpeedScaling(17),
    WavesShape(18),
    WavesOctaves(19),
    WavesMix(20),
    WavesPull(21),
    WavesDirectionIncrement(22),
    MidtonesContrast(23),
    HighlightsContrast(24),
    ShadowsContrast(25),
    HighlightsGain(26),
    HighlightsGamma(27),
    HighlightsOffset(28),
    HighlightsSaturation(29),
    MidtonesGain(30),
    MidtonesGamma(31),
    MidtonesOffset(32),
    MidtonesSaturation(33),
    ShadowsGain(34),
    ShadowsGamma(35),
    ShadowsOffset(36),
    ShadowsSaturation(37),
    HighlightsMin(38),
    ShadowsMax(39),
    Temperature(40),
    SunColor(41),
    SunIlluminance(42),
    MoonColor(43),
    MoonIlluminance(44),
    FlashColor(45),
    FlashIlluminance(46),
    AmbientColor(47),
    AmbientIlluminance(48),
    EmissiveDesaturation(49),
    SkyIntensity(50),
    OrbitalOffsetDegrees(51),
    ;

    private static final Int2ObjectMap<GraphicsOverrideParameterType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (GraphicsOverrideParameterType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) {
                BY_VALUE.put(value.value, value);
            }
        }
    }

    public static GraphicsOverrideParameterType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static GraphicsOverrideParameterType getByValue(final int value, final GraphicsOverrideParameterType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    public static GraphicsOverrideParameterType getByName(final String name) {
        for (GraphicsOverrideParameterType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public static GraphicsOverrideParameterType getByName(final String name, final GraphicsOverrideParameterType fallback) {
        for (GraphicsOverrideParameterType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return fallback;
    }

    private final int value;

    GraphicsOverrideParameterType(final GraphicsOverrideParameterType value) {
        this(value.value);
    }

    GraphicsOverrideParameterType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
