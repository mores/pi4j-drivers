package com.pi4j.drivers.input.rotary.adafruit5880;

/**
 * Internal constants for register address, values and masks
 * https://github.com/adafruit/Adafruit_Seesaw/blob/985b41efae3d9a8cba12a7b4d9ff0d226f9e0759/Adafruit_seesaw.h
 */
class Adafruit5880Constants {

    // the knob functionality
    public static final byte ENCODER_BASE = 0x11;
    public static final byte ENCODER_INTERUPTSET = 0x10;
    public static final byte ENCODER_INTERUPTCLR = 0x20;
    public static final byte ENCODER_POSITION = 0x30;
    public static final byte ENCODER_DELTA = 0x40;

    // the button functionality
    public static final byte GPIO_BASE = 0x01;
    public static final byte GPIO_BULK = 0x04;
    public static final byte GPIO_INTERUPTSET = 0x08;
    public static final byte GPIO_INTERUPTCLR = 0x09;

    public static final byte NEOPIXEL_BASE = 0x0E;
    public static final byte NEOPIXEL_BUF = 0x04;
    public static final byte NEOPIXEL_BUF_LENGTH = 0x03;
    public static final byte NEOPIXEL_PIN = 0x01;
    public static final byte NEOPIXEL_SHOW = 0x05;

    // seesaw
    public static final byte STATUS_BASE = 0x00;
    public static final byte STATUS_HW_ID = 0x01;
    public static final byte STATUS_VERSION = 0x02;
    public static final byte STATUS_SWRST = 0x7F;

}
