package com.pi4j.drivers.display;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseGraphicsDisplayComponent {
    private static final int MAX_SPI_TRANSFER_SIZE = 65535;

    private static Logger log = LoggerFactory.getLogger(BaseGraphicsDisplayComponent.class);

    protected final GraphicsDisplayDriver driver;
    private final byte[] spiBuffer;

    public BaseGraphicsDisplayComponent(GraphicsDisplayDriver driver) {
        this.driver = driver;
        spiBuffer = new byte[Math.min(MAX_SPI_TRANSFER_SIZE,
                (driver.getDisplayInfo().getHeight() * driver.getDisplayInfo().getWidth() * driver.getDisplayInfo().getPixelFormat().getBitCount() + 7) / 8)];
    }

    // it is possible that rgb888pixels contains more than will fit
    // we will just ignore them
    public void drawImage(int x, int y, int width, int height, int[] rgb888pixels) {
        log.debug("drawImage: {},{} \t {} x {} \t {}", x, y, width, height, rgb888pixels.length);
        PixelFormat pixelFormat = driver.getDisplayInfo().getPixelFormat();

        int bitsPerRow = width * pixelFormat.getBitCount();
        int bitOffset = 0;
        for (int i = 0; i < height; i++) {
            bitOffset += pixelFormat.writeRgb(rgb888pixels, width * i, spiBuffer, bitOffset, width);
            // Transfer if the last row is reached or the next row would overflow the buffer.
            if (i == height - 1 || bitOffset + bitsPerRow > spiBuffer.length * 8) {
                int rows = bitOffset / bitsPerRow;
                driver.setPixels(x, y + i + 1 - rows, width, rows, spiBuffer);
                bitOffset = 0;
            }
        }
    }

    public void fillRect(int x, int y, int width, int height, int rgb888) throws java.io.IOException {
        PixelFormat pixelFormat = driver.getDisplayInfo().getPixelFormat();

        int bitsPerRow = width * pixelFormat.getBitCount();
        int rowCount = spiBuffer.length * 8 / bitsPerRow;

        pixelFormat.fillRgb(spiBuffer, 0, width * rowCount, rgb888);

        for (int i = 0; i < height; i += rowCount) {
            driver.setPixels(
                    x,
                    y + i,
                    width,
                    Math.min(rowCount, height - i),
                    spiBuffer);
        }
    }
}
