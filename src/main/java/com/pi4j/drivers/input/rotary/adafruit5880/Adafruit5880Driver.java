package com.pi4j.drivers.input.rotary.adafruit5880;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2C;

/*
 *
 */

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

        byte[] pin = new byte[3];
        pin[0] = (byte) Adafruit5880Constants.NEOPIXEL_BASE;
        pin[1] = (byte) Adafruit5880Constants.NEOPIXEL_PIN;
        pin[2] = (byte) 0x06;
        i2c.write(pin);

        byte[] bufLength = new byte[4];
        bufLength[0] = (byte) Adafruit5880Constants.NEOPIXEL_BASE;
        bufLength[1] = (byte) Adafruit5880Constants.NEOPIXEL_BUF_LENGTH;
        bufLength[2] = (byte) 0x00;
        bufLength[3] = (byte) 0x03;
        i2c.write(bufLength);

        setPosition(0);
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

    public int getPosition() {

        i2c.writeRegister((byte) Adafruit5880Constants.ENCODER_BASE, (byte) Adafruit5880Constants.ENCODER_POSITION);

        try {
            Thread.sleep(8);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer buf = i2c.readRegisterByteBuffer(Adafruit5880Constants.ENCODER_BASE, 4);
        return buf.getInt();
    }

    public boolean setPosition(int pos) {

        byte[] data = new byte[6];
        data[0] = (byte) Adafruit5880Constants.ENCODER_BASE;
        data[1] = (byte) Adafruit5880Constants.ENCODER_POSITION;

        data[5] = (byte) (pos & 0xff);
        data[4] = (byte) ((pos >> 8) & 0xff);
        data[3] = (byte) ((pos >> 16) & 0xff);
        data[2] = (byte) ((pos >> 24) & 0xff);
        i2c.write(data);
        return true;
    }
}
