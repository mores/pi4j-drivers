package com.pi4j.drivers.display;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PixelFormatTest {

    @Test
    public void testRgb444() {
        byte[] target = new byte[3];

        PixelFormat format = PixelFormat.RGB_444;

        int offset = 0;
        offset += format.writeRgb(0x0aabbcc, target, offset);
        offset += format.writeRgb(0x0112233, target, offset);

        assertEquals(24, offset);
        assertEquals((byte) 0xab, target[0]);
        assertEquals((byte) 0xc1, target[1]);
        assertEquals((byte) 0x23, target[2]);
    }


    @Test
    public void testRgb565() {
        byte[] target = new byte[4];

        PixelFormat format = PixelFormat.RGB_565;

        int offset = 0;
        offset += format.writeRgb(0x0ff00ff, target, offset);
        offset += format.writeRgb(0x0f0f0f0, target, offset);

        assertEquals(32, offset);
        assertEquals((byte) 0b11111_000, target[0]);
        assertEquals((byte) 0b000_11111, target[1]);

        assertEquals((byte) 0b11110_111, target[2]);
        assertEquals((byte) 0b100_11110, target[3]);
    }
}
