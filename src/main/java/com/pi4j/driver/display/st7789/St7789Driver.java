package com.pi4j.driver.display.st7789;

import com.pi4j.driver.display.GraphicsDisplayDriver;
import com.pi4j.driver.display.PixelFormat;
import com.pi4j.driver.display.DisplayInfo;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.Spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Tested on Adafruit 1.54" 240x240 Wide Angle TFT LCD Display with MicroSD - ST7789 with EYESPI Connector
 * https://www.adafruit.com/product/3787
 */

public class St7789Driver implements GraphicsDisplayDriver {

    private static Logger log = LoggerFactory.getLogger(St7789Driver.class);

    // This chip controls 240x320
    // An offset of 80 allows it to control 240x240
    private final int OFFSET = 80;
    private final int WIDTH = 240;
    private final int HEIGHT = 240;

    private static final int SWRESET = 0x01;
    private static final int SLPOUT = 0x11;
    private static final int NORON = 0x13;
    private static final int INVON = 0x21;
    private static final int DISPON = 0x29;
    private static final int CASET = 0x2A;
    private static final int RASET = 0x2B;
    private static final int RAMWR = 0x2C;
    private static final int MADCTL = 0x36;
    private static final int COLMOD = 0x3A;

    private static final int COLMOD_RGB_65K = 0x50;
    private static final int COLMOD_CONTROL_12BIT = 0x03;
    private static final int COLMOD_CONTROL_16BIT = 0x05;

    private static final int MADCTL_RGB_ORDER = 0x00;
    private static final int MADCTL_BGR_ORDER = 0x08;

    private Spi spi;
    private DigitalOutput dc;
    private PixelFormat pixelFormat;

    public St7789Driver(Spi spi, DigitalOutput dc, PixelFormat pixelFormat) {

        this.spi = spi;
        this.dc = dc;
        this.pixelFormat = pixelFormat;

        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws java.io.IOException, InterruptedException {

        command(SWRESET);
        Thread.sleep(200);

        command(SLPOUT);

        command(COLMOD);
        if (PixelFormat.RGB_444 == pixelFormat) {
            data(COLMOD_RGB_65K | COLMOD_CONTROL_12BIT);
        } else {
            data(COLMOD_RGB_65K | COLMOD_CONTROL_16BIT);
        }

        command(MADCTL);
        data(MADCTL_BGR_ORDER);

        command(CASET); // Column addr set
        byte[] cols = new byte[4];
        cols[0] = 0x00;
        cols[1] = 0x00;
        cols[2] = (byte) (WIDTH >> 8);
        cols[3] = (byte) (WIDTH & 0xff);
        data(cols);

        command(RASET); // Row addr set
        byte[] rows = new byte[4];
        rows[0] = 0x00;
        rows[1] = 0x50;
        rows[2] = (byte) ((OFFSET + HEIGHT) >> 8);
        rows[3] = (byte) ((OFFSET + HEIGHT) & 0xff);
        data(rows);

        command(INVON);

        command(NORON);

        command(DISPON);

        command(MADCTL);
        data(0xC0);

    }

    private void command(int x) {

        if (x < 0 || x > 0xff) {
            throw new IllegalArgumentException("ST7789 bad command value " + x);
        }

        log.trace("Command: {}", x);

        dc.off();
        byte[] buffer = new byte[1];
        buffer[0] = (byte) x;
        spi.write(buffer);
    }

    private void data(int x) {

        if (x < 0 || x > 0xff) {
            throw new IllegalArgumentException("ST7789 bad data value " + x);
        }

        byte[] buffer = new byte[1];
        buffer[0] = (byte) x;

        data(buffer);
    }

    private void data(byte[] x) {

        String raw = java.util.HexFormat.of().formatHex(x);
        if (raw.length() > 100) {
            log.trace("Data: {} {}", x.length, raw.substring(0, 80));
        } else {
            log.trace("Data: {} {}", x.length, raw);
        }

        dc.on();
        spi.write(x);
        dc.off();
    }

    @Override
    public DisplayInfo getDisplayInfo() {

        return new DisplayInfo(WIDTH, HEIGHT, pixelFormat);
    }

    @Override
    public void setPixels(int x, int y, int width, int height, byte[] data) {

        log.trace("setPixels {}", data.length);
        command(CASET); // Column addr set
        byte[] cols = new byte[4];
        cols[0] = (byte) (x >> 8);
        cols[1] = (byte) (x & 0xFF);
        cols[2] = (byte) (x + width - 1 >> 8);
        cols[3] = (byte) (x + width - 1 & 0xff);
        data(cols);

        command(RASET); // Row addr set
        byte[] rows = new byte[4];
        rows[0] = (byte) (OFFSET + y >> 8);
        rows[1] = (byte) (OFFSET + y & 0xFF);
        rows[2] = (byte) ((OFFSET + y + height - 1) >> 8);
        rows[3] = (byte) ((OFFSET + y + height - 1) & 0xff);
        data(rows);

        command(RAMWR); // write to RAM
        data(data);
    }
}
