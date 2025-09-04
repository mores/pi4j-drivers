package com.pi4j.drivers.display;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PixelFormatTest {

    @Test
    public void testFromRgb444() {
        assertEquals(0xabc, PixelFormat.RGB_444.fromRgb(0xaabbcc));
        assertEquals(0xabc, PixelFormat.RGB_444.fromRgb(0xa0b0c0));
        assertEquals(0xabc, PixelFormat.RGB_444.fromRgb(0xafbfcf));

        assertEquals(0x000, PixelFormat.RGB_444.fromRgb(0));
        assertEquals(0x888, PixelFormat.RGB_444.fromRgb(0x888888));
        assertEquals(0xfff, PixelFormat.RGB_444.fromRgb(0xffffff));
    }

    @Test
    public void testFromRgb565() {
        assertEquals(0b11111_000000_11111, PixelFormat.RGB_565.fromRgb(0x0ff00ff));
        assertEquals(0b11110_111100_11110, PixelFormat.RGB_565.fromRgb(0x0f0f0f0));

        assertEquals(0, PixelFormat.RGB_565.fromRgb(0));
        assertEquals(0b11111_111111_11111, PixelFormat.RGB_565.fromRgb(0xffffff));
    }

    @Test
    public void testWriteRgb444() {
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
    public void testWriteRgb565() {
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

    @Test
    public void testWriteRgbArray444() {
        byte[] target = new byte[6];

        PixelFormat format = PixelFormat.RGB_444;

        int offset = format.writeRgb(new int[] {0x0aabbcc, 0x0112233}, 0, target, 0, 2);

        assertEquals(24, offset);
        assertEquals((byte) 0xab, target[0]);
        assertEquals((byte) 0xc1, target[1]);
        assertEquals((byte) 0x23, target[2]);
    }

    @Test
    public void testWriteRgbArray565() {
        byte[] target = new byte[4];

        PixelFormat format = PixelFormat.RGB_565;

        int offset = format.writeRgb(new int[] {0x0ff00ff, 0x0f0f0f0}, 0, target, 0, 2);

        assertEquals(32, offset);
        assertEquals((byte) 0b11111_000, target[0]);
        assertEquals((byte) 0b000_11111, target[1]);

        assertEquals((byte) 0b11110_111, target[2]);
        assertEquals((byte) 0b100_11110, target[3]);
    }

    @Test
    public void testFillRgb444() {
        byte[] target = new byte[3];

        PixelFormat format = PixelFormat.RGB_444;

        int offset = format.fillRgb(target, 0, 2, 0xaabbcc);

        assertEquals(24, offset);
        assertEquals((byte) 0xab, target[0]);
        assertEquals((byte) 0xca, target[1]);
        assertEquals((byte) 0xbc, target[2]);
    }

    @Test
    public void testFillRgb565() {
        byte[] target = new byte[4];

        PixelFormat format = PixelFormat.RGB_565;

        int offset = format.fillRgb(target, 0, 2, 0x0ff00ff);

        assertEquals(32, offset);
        assertEquals((byte) 0b11111_000, target[0]);
        assertEquals((byte) 0b000_11111, target[1]);
        assertEquals((byte) 0b11111_000, target[2]);
        assertEquals((byte) 0b000_11111, target[3]);
    }
}
