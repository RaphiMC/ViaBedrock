// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GraphicsOverrideParameterType {

    /**
     * Sent to set the sky zenith color
     */
    SkyZenithColor(0),
    /**
     * Sent to set the sky horizon color
     */
    SkyHorizonColor(1),
    /**
     * Sent to set the horizon blend min
     */
    HorizonBlendMin(2),
    /**
     * Sent to set the horizon blend max
     */
    HorizonBlendMax(3),
    /**
     * Sent to set the horizon blend start
     */
    HorizonBlendStart(4),
    /**
     * Sent to set the horizon blend mie start
     */
    HorizonBlendMieStart(5),
    /**
     * Sent to set the rayleigh strength
     */
    RayleighStrength(6),
    /**
     * Sent to set the sun mie strength
     */
    SunMieStrength(7),
    /**
     * Sent to set the moon mie strength
     */
    MoonMieStrength(8),
    /**
     * Sent to set the sun glare shape
     */
    SunGlareShape(9),
    /**
     * Sent to set the water chlorophyll value
     */
    Chlorophyll(10),
    /**
     * Sent to set the water CDOM value
     */
    CDOM(11),
    /**
     * Sent to set the water SuspendedSediment value
     */
    SuspendedSediment(12),
    /**
     * Sent to set the water waves depth value
     */
    WavesDepth(13),
    /**
     * Sent to set the water waves frequency value
     */
    WavesFrequency(14),
    /**
     * Sent to set the water waves frequency scaling value
     */
    WavesFrequencyScaling(15),
    /**
     * Sent to set the water waves speed value
     */
    WavesSpeed(16),
    /**
     * Sent to set the water waves speed scaling value
     */
    WavesSpeedScaling(17),
    /**
     * Sent to set the water waves shape value
     */
    WavesShape(18),
    /**
     * Sent to set the water waves octaves value
     */
    WavesOctaves(19),
    /**
     * Sent to set the water waves mix value
     */
    WavesMix(20),
    /**
     * Sent to set the water waves pull value
     */
    WavesPull(21),
    /**
     * Sent to set the water waves direction increment value
     */
    WavesDirectionIncrement(22),
    /**
     * Sent to set the color grading midtones contrast value
     */
    MidtonesContrast(23),
    /**
     * Sent to set the color grading highlights contrast value
     */
    HighlightsContrast(24),
    /**
     * Sent to set the color grading shadows contrast value
     */
    ShadowsContrast(25),
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
