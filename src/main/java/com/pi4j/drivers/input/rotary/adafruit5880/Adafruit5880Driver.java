package com.pi4j.drivers.input.rotary.adafruit5880;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2C;

public class Adafruit5880Driver {

    private static Logger log = LoggerFactory.getLogger(Adafruit5880Driver.class);

    private I2C i2c;

    public Adafruit5880Driver(I2C i2c) {

        this.i2c = i2c;

        i2c.writeRegister((byte) Adafruit5880Constants.STATUS_BASE, (byte) Adafruit5880Constants.STATUS_HW_ID);
        try {
            Thread.sleep(8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        byte chipId = (byte) i2c.readRegister((byte) Adafruit5880Constants.STATUS_BASE);
        log.info("chipId: " + chipId);
    }

    public void setPixel(int color) throws Exception {

        byte[] msg = new byte[7];
        msg[0] = (byte) Adafruit5880Constants.NEOPIXEL_BASE;
        msg[1] = (byte) Adafruit5880Constants.NEOPIXEL_BUF;
        msg[2] = 0x00;
        msg[3] = 0x00;
        msg[4] = (byte) ((color & 0x00FF00) >> 8);
        msg[5] = (byte) ((color & 0xFF0000) >> 16);
        msg[6] = (byte) (color & 0x0000FF);
        i2c.write(msg);

        byte[] show = new byte[2];
        show[0] = (byte) Adafruit5880Constants.NEOPIXEL_BASE;
        show[1] = (byte) Adafruit5880Constants.NEOPIXEL_SHOW;
        i2c.write(show);

    }
}
