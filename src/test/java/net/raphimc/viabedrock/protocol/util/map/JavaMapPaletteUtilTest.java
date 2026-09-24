package net.raphimc.viabedrock.protocol.util.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaMapPaletteUtilTest {

    @Test
    void convertsArgbWithoutSwappingRedAndBlue() {
        assertEquals(18, JavaMapPaletteUtil.convertToJavaPalette(new int[]{0xFFFF0000})[0]);
    }

    @Test
    void keepsOpaqueDarkPixelsVisible() {
        final short[] palette = JavaMapPaletteUtil.convertToJavaPalette(new int[]{0x00000000, 0xFF000000});
        assertEquals(0, palette[0]);
        assertTrue(palette[1] >= 4);
    }
}
