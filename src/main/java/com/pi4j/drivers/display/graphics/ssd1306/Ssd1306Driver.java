package com.pi4j.drivers.display.graphics.ssd1306;

import com.pi4j.drivers.display.graphics.GraphicsDisplayDriver;
import com.pi4j.drivers.display.graphics.GraphicsDisplayInfo;
import com.pi4j.io.i2c.I2C;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssd1306Driver implements GraphicsDisplayDriver {

    private static Logger log = LoggerFactory.getLogger(Ssd1306Driver.class);

    private static final int COMMAND_DISPLAY_ON = 0xAE;
    private static final int COMMAND_DISPLAY_OFF = 0xAF;

    private static final int COMMAND_SET_ADDRESS_LINE = 0x40;

    private static final int COMMAND_SET_MEM_ADDRESS_MODE = 0x20;
    private static final int COMMAND_SET_MEM_ADDRESS_MODE_HORZ = 0x00;
    private static final int COMMAND_SET_MEM_ADDRESS_MODE_VERT = 0x01;
    private static final int COMMAND_SET_COLUMN_ADDRESS = 0x21;
    private static final int COMMAND_SET_DISPLAY_OFFSET = 0xD3;

    private static final int DISABLE_DISPLAY = 0x00;
    private static final int ENABLE_DISPLAY = 0x1;
    private static final int WITH_ONE_COMMAND = 0x00;
    private static final int WITH_DATA_ONLY = 0x40;

    private final I2C i2c;

    public Ssd1306Driver(I2C i2c) {

        this.i2c = i2c;

        init();
    }

    private void init() {

        command(COMMAND_DISPLAY_ON | DISABLE_DISPLAY);
        // Set MUX Ratio [$A8, $3F]
        command(0xA8, 0x3f);

        // Set display offset [$D3, $00]
        command(0xD3, 0x00);

        // Set segment re-map $A0 / $A1
        command(0xA0);

        // Set COM output scan direction $C0 / $C8
        command(0xC0);

        // Set COM pin hardware configuration [$DA, $12] 128x64 -- was 00
        command(0xDA, 0x12);

        // Set contrast [$81, $7F]--- 8f
        command(0x81, 0x7f);

        // Set precharge [$D9, $22]
        command(0xD9, 0x22);

        // voltage com detect $DB 20
        command(0xDB, 0x20);

        // Set Oscillator frequency [$D5, $80]
        command(0xD5, 0x80);

        // Enable charge pump [$8D, $14]
        command(0x8D, 0x14);

        // Resume the display $A4
        command(0xA4);

        // normal display $A6/$A7(inverse)
        command(0xA6);

        // Turn the display on $AF
        command(COMMAND_DISPLAY_ON | ENABLE_DISPLAY);

        command(COMMAND_SET_MEM_ADDRESS_MODE, COMMAND_SET_MEM_ADDRESS_MODE_HORZ);

        setColumnAddress(0, 127);
    }

    private void command(int x) {

        log.info("Command: {} {}", x, String.format("0x%08x ", x));

        byte[] buf = new byte[2];
        buf[0] = WITH_ONE_COMMAND;
        buf[1] = (byte) x;
        i2c.write(buf);
    }

    private void command(int x1, int x2) {

        log.info("Command: {} {} {} {}", x1, x2, String.format("0x%08x ", x1), String.format("0x%08x ", x2));

        byte[] buf = new byte[3];
        buf[0] = WITH_ONE_COMMAND;
        buf[1] = (byte) x1;
        buf[2] = (byte) x2;
        i2c.write(buf);
    }

    private void setColumnAddress(int start, int end) {
        byte[] buf = new byte[4];
        buf[0] = WITH_ONE_COMMAND;
        buf[1] = COMMAND_SET_COLUMN_ADDRESS;
        buf[2] = (byte) (start & 0x7F);
        buf[3] = (byte) (end & 0x7F);
        this.i2c.write(buf);
    }

    @Override
    public GraphicsDisplayInfo getDisplayInfo() {
        return null;
    }

    @Override
    public void setPixels(int x, int y, int width, int height, byte[] data) {

        log.info("setPixels {} {} {} {} {}", x, y, width, height, data.length);

        command(COMMAND_SET_ADDRESS_LINE | (byte) (0x00 & 0x3F));

        byte[] buf = new byte[data.length + 1];
        buf[0] = WITH_DATA_ONLY;
        System.arraycopy(data, 0, buf, 1, data.length);

        i2c.write(buf, buf.length);
    }

    @Override
    public void close() {
    }
}
