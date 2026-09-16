// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!
package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum GraphicsOverrideParameterType {

    skyzenithcolor(0),
    skyhorizoncolor(1),
    horizonblendmin(2),
    horizonblendmax(3),
    horizonblendstart(4),
    horizonblendmiestart(5),
    rayleighstrength(6),
    sunmiestrength(7),
    moonmiestrength(8),
    sunglareshape(9),
    chlorophyll(10),
    cdom(11),
    suspendedsediment(12),
    wavesdepth(13),
    wavesfrequency(14),
    wavesfrequencyscaling(15),
    wavesspeed(16),
    wavesspeedscaling(17),
    wavesshape(18),
    wavesoctaves(19),
    wavesmix(20),
    wavespull(21),
    wavesdirectionincrement(22),
    midtonescontrast(23),
    highlightscontrast(24),
    shadowscontrast(25),
    highlightsgain(26),
    highlightsgamma(27),
    highlightsoffset(28),
    highlightssaturation(29),
    midtonesgain(30),
    midtonesgamma(31),
    midtonesoffset(32),
    midtonessaturation(33),
    shadowsgain(34),
    shadowsgamma(35),
    shadowsoffset(36),
    shadowssaturation(37),
    highlightsmin(38),
    shadowsmax(39),
    temperature(40),
    suncolor(41),
    sunilluminance(42),
    mooncolor(43),
    moonilluminance(44),
    flashcolor(45),
    flashilluminance(46),
    ambientcolor(47),
    ambientilluminance(48),
    emissivedesaturation(49),
    skyintensity(50),
    orbitaloffsetdegrees(51),
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
