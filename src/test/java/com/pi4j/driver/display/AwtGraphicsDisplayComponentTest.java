package com.pi4j.driver.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.driver.display.DisplayInfo;
import com.pi4j.driver.display.PixelFormat;

public class AwtGraphicsDisplayComponentTest {

    private static Logger log = LoggerFactory.getLogger(AwtGraphicsDisplayComponentTest.class);

    // 12 bit test
    @Test
    public void testRgb888toRgb444() throws IOException {

        FakeDisplayDriver display = new FakeDisplayDriver(new DisplayInfo(10, 10, PixelFormat.RGB_444));
        AwtGraphicsDisplayComponent mockDisplay = new AwtGraphicsDisplayComponent(display);

        BufferedImage img = new BufferedImage(1, 48, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = img.createGraphics();

        g2d.setPaint(java.awt.Color.RED);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.dispose();
        mockDisplay.display(img);

        byte[] data = display.getData();
        byte zeroZero = data[0];

        log.trace("Size of Data: {}", data.length);
        assertEquals((byte) 0xF0, data[0]);
        assertEquals(48 * 12 / 8, data.length);
    }

    // 16 bit test
    // @Test
    public void testRgb888toRgb565() throws IOException {

        FakeDisplayDriver display = new FakeDisplayDriver(new DisplayInfo(10, 10, PixelFormat.RGB_565));
        AwtGraphicsDisplayComponent mockDisplay = new AwtGraphicsDisplayComponent(display);

        BufferedImage img = new BufferedImage(1, 48, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = img.createGraphics();

        g2d.setPaint(java.awt.Color.RED);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.dispose();
        mockDisplay.display(img);

        byte[] data = display.getData();
        byte zeroZero = data[0];

        log.trace("Size of Data: {}", data.length);
        assertEquals((byte) 0xF8, data[0], "First pixel is unexpected");
        assertEquals(10 * 10 * 16 / 8, data.length, "Length of data is unexpected");
    }

}
