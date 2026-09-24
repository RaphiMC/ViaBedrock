package net.raphimc.viabedrock.protocol.util.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaMapPaletteUtilTest {

    @Test
    void convertsBedrockRgbaWirePixelToJavaRed() {
        assertEquals(18, JavaMapPaletteUtil.convertToJavaPalette(new int[]{0xFF0000FF})[0]);
    }

    @Test
    void keepsOpaqueDarkPixelsVisible() {
        final short[] palette = JavaMapPaletteUtil.convertToJavaPalette(new int[]{0x00000000, 0xFF000000});
        assertEquals(0, palette[0]);
        assertTrue(palette[1] >= 4);
    }

    @Test
    void distinguishesPaletteColorsWithNearbyRgbValues() {
        final short[] palette = JavaMapPaletteUtil.convertToJavaPalette(new int[]{0xFF696969, 0xFF6C6C6C});
        assertEquals(15, palette[0]);
        assertEquals(88, palette[1]);
    }
}
