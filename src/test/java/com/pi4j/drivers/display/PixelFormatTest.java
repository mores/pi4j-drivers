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

        assertEquals((byte) 0xab, target[0]);
        assertEquals((byte) 0xc1, target[1]);
        assertEquals((byte) 0x23, target[2]);
    }

}
