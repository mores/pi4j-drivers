package com.pi4j.drivers.display.graphics;

public class GraphicsDisplayInfo {

    private final int width;
    private final int height;
    private final PixelFormat pixelFormat;

    /** x-coordinates must be a multiple of this value when sending data to the driver. */
    private final int xGranularity;

    private static int granularityForBits(int bitCount) {
        int xGranularity = 1;
        while ((xGranularity * bitCount) % 8 != 0) {
            xGranularity *= 2;
        }
        return xGranularity;
    }

    public GraphicsDisplayInfo(int width, int height, PixelFormat pixelFormat, int xGranularity) {
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.xGranularity = xGranularity;
    }

    public GraphicsDisplayInfo(int width, int height, PixelFormat pixelFormat) {
        this(width, height, pixelFormat, granularityForBits(pixelFormat.getBitCount()));
    }

        /** The width of the display in pixel. */
    public int getWidth() {
        return width;
    }

    /** The height of the display in pixel. */
    public int getHeight() {
        return height;
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    /** x-coordinates must be a multiple of this value when sending data to the driver. */
    public int getXGranularity() {
        return xGranularity;
    }

}
