package com.pi4j.driver.display;

import java.awt.Color;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.driver.display.DisplayInfo;
import com.pi4j.driver.display.PixelFormat;

public class BaseGraphicsDisplayComponentTest {

    private static Logger log = LoggerFactory.getLogger(BaseGraphicsDisplayComponentTest.class);

    // 16 bit test
    @Test
    public void testRgb888toRgb565() throws IOException {

        FakeDisplayDriver display = new FakeDisplayDriver(new DisplayInfo(100, 100, PixelFormat.RGB_565));
        BaseGraphicsDisplayComponent mockDisplay = new BaseGraphicsDisplayComponent(display);
        mockDisplay.fillRect(0, 0, 48, 1, Color.RED.getRGB());

        byte[] data = display.getData();
        byte zeroZero = data[0];

        log.trace("Size of Data: {}", data.length);
        assertEquals((byte) 0xF8, data[0]);
        assertEquals(48 * 16 / 8, data.length);
    }

}
