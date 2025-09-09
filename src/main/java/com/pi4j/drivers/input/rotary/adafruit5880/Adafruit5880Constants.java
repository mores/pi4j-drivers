package com.pi4j.drivers.input.rotary.adafruit5880;

/**
 * Internal constants for register address, values and masks
 */
class Adafruit5880Constants {

    public static final short ENCODER_BASE = 0x11;
    public static final short ENCODER_INTENSET = 0x10;
    public static final short ENCODER_INTENCLR = 0x20;
    public static final short ENCODER_POSITION = 0x30;
    public static final short ENCODER_DELTA = 0x40;

    public static final short GPIO_BASE = 0x01;
    public static final short GPIO_BULK = 0x04;

    public static final short NEOPIXEL_BASE = 0x0E;
    public static final short NEOPIXEL_BUF = 0x04;
    public static final short NEOPIXEL_BUF_LENGTH = 0x03;
    public static final short NEOPIXEL_PIN = 0x01;
    public static final short NEOPIXEL_SHOW = 0x05;

    public static final short STATUS_BASE = 0x00;
    public static final short STATUS_HW_ID = 0x01;
    public static final short STATUS_VERSION = 0x02;

}
