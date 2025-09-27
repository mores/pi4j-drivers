package com.pi4j.drivers.display.graphics.ws281x;

import com.pi4j.drivers.display.graphics.GraphicsDisplayDriver;
import com.pi4j.drivers.display.graphics.GraphicsDisplayInfo;
import com.pi4j.drivers.display.graphics.PixelFormat;
import com.pi4j.io.spi.Spi;

import java.io.IOException;

/**
 * Implements a driver for WS 281x LED strips using a SPI interface. Note that the baud rate of the
 * SPI channel needs to be set to SPI_BAUD.
 * <p>
 * This driver is based on timing information form this article:
 * https://wp.josh.com/2014/05/13/ws2812-neopixels-are-not-so-finicky-once-you-get-to-know-them/
 */
public class Ws281xDriver implements GraphicsDisplayDriver {
    /** The number of color channels (r, g, b); each using one byte. */
    private static final int COLOR_CHANNELS = 3;
    /**
     * How many bits it takes to send a single bit via SPI. 4 Bit seem to be a reasonable compromise between
     * timing precision and keeping the amount of data and the frequency low. Finding a frequency that would work
     * for three bit seemed tricky, as the minimum "low" phase after the variable length bit pulse is significantly
     * longer than the 1-bit pulse width.
     */
    private static final int BIT_STRETCH = 4;
    /** The baud rate the SPI channel needs to be configured to. */
    public static final int SPI_BAUD = 800_000 * BIT_STRETCH;

    /** A buffer of the transformed pixels in the format they will be sent over SPI */
    private final byte[] spiBuffer;
    private final GraphicsDisplayInfo displayInfo;
    private final Spi spi;

    /**
     * The baud rate of the SPI channel handed in needs to configured to SPI_BAUD.
     * <p>
     * For two-dimensional LED strip arrangement, the driver assumes that LED index 0 is at coordinates (0,0)
     * and the start LED index of each subsequent row is at y * width.
     */
    public Ws281xDriver(Spi spi, int width, int height) {
        this.spi = spi;
        spiBuffer = new byte[width * height * COLOR_CHANNELS * BIT_STRETCH];
        displayInfo = new GraphicsDisplayInfo(width, height, PixelFormat.RGB_888);
    }

    @Override
    public GraphicsDisplayInfo getDisplayInfo() {
        return displayInfo;
    }

    @Override
    public void setPixels(int x, int y, int width, int height, byte[] bytes) {
        int src = 0;
        int lastChangedByte = 0;
        for (int i = 0; i < height; i++) {
            int dst = ((y + i) * displayInfo.getWidth() + x) * COLOR_CHANNELS * BIT_STRETCH;
            for (int j = 0; j < width * COLOR_CHANNELS; j++) {
                byte b = bytes[src++];
                for (int bitIdex = 0; bitIdex < 8; bitIdex += 2) {
                    byte newValue = (byte) ((((b << bitIdex) & 0x80) == 0 ? 0b1000_0000 : 0b1100_0000)
                                            | (((b << bitIdex) & 0x40) == 0 ? 0b1000 : 0b1100));
                    if (newValue != spiBuffer[dst]) {
                        spiBuffer[dst] = newValue;
                        lastChangedByte = dst;
                    }
                    dst++;
                }
            }
        }
        // We always need to start at 0, but we only need to send up to the last changed pixel.
        // Dividing and then multiplying makes sure we always send full pixel data and don't stop somewhere in the
        // middle of a pixel.
        int lastChangedPixel = lastChangedByte / COLOR_CHANNELS / BIT_STRETCH;
        if (lastChangedPixel > 0) {
            spi.write(spiBuffer, 0, lastChangedPixel * COLOR_CHANNELS * BIT_STRETCH);
        }
    }

    @Override
    public void close() {
        spi.close();
    }
}
