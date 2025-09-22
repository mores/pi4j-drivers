package com.pi4j.drivers.display.graphics;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDisplayComponentTest {

    // 12 bit test
    @Test
    public void testRgb888toRgb444() throws IOException {
        FakeDisplayDriver driver = new FakeDisplayDriver(new DisplayInfo(100, 100, PixelFormat.RGB_444));
        BaseDisplayComponent display = new BaseDisplayComponent(driver);
        display.fillRect(0, 0, 48, 1, Color.RED.getRGB());
        display.flush();

        byte[] data = driver.getData();

        assertEquals((byte) 0xF0, data[0]);
        assertEquals(100 * 100 * 3 / 2, data.length);
    }

    // 16 bit test
    @Test
    public void testRgb888toRgb565() throws IOException {
        FakeDisplayDriver driver = new FakeDisplayDriver(new DisplayInfo(100, 100, PixelFormat.RGB_565));
        BaseDisplayComponent display = new BaseDisplayComponent(driver);
        display.setTransferDelayMillis(0);
        display.fillRect(0, 0, 48, 1, Color.RED.getRGB());

        byte[] data = driver.getData();

        assertEquals(100 * 100 * 2, data.length);
        assertEquals((byte) 0xF8, data[0]);
    }

    @Test
    public void testSetPixel() {
        FakeDisplayDriver driver = new FakeDisplayDriver(new DisplayInfo(100, 100, PixelFormat.RGB_888));
        BaseDisplayComponent display = new BaseDisplayComponent(driver);
        display.setTransferDelayMillis(0);
        display.setPixel(10, 10, 0x112233);

        byte[] data = driver.getData();
        assertEquals(100 * 100 * 3, data.length);

        int pos = (10 * 100 + 10) * 3;
        System.out.println(Arrays.toString(data));
        assertEquals(0x11, data[pos]);
        assertEquals(0x22, data[pos+1]);
        assertEquals(0x33, data[pos+2]);
    }
}
