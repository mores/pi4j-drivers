package com.pi4j.drivers.display.graphics;

public class GraphicsDisplayInfo {

    private final int width;
    private final int height;
    private final PixelFormat pixelFormat;

    public GraphicsDisplayInfo(int width, int height, PixelFormat pixelFormat) {
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
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
}
