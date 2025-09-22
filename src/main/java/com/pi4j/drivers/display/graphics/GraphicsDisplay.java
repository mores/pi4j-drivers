package com.pi4j.drivers.display.graphics;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class GraphicsDisplay {
    // TODO(https://github.com/Pi4J/pi4j/issues/475): Remove or update this limitation.
    private static final int MAX_TRANSFER_SIZE = 4000;

    protected final GraphicsDisplayDriver driver;
    private final Object lock = new Object();
    private final int[] displayBuffer;
    private final byte[] transferBuffer;
    private final Timer timer = new Timer();

    private int modifiedXMax = Integer.MIN_VALUE;
    private int modifiedXMin = Integer.MAX_VALUE;
    private int modifiedYMax = Integer.MIN_VALUE;
    private int modifiedYMin = Integer.MAX_VALUE;
    private TimerTask pendingUpdate = null;
    private int transferDelayMillis = 20;
    private final int displayWidth;
    private final int displayHeight;

    public GraphicsDisplay(GraphicsDisplayDriver driver) {
        this.driver = driver;
        displayWidth = driver.getDisplayInfo().getWidth();
        displayHeight = driver.getDisplayInfo().getHeight();
        displayBuffer = new int[displayWidth * displayHeight];
        transferBuffer = new byte[Math.min(
                MAX_TRANSFER_SIZE,
                (displayWidth * displayHeight * driver.getDisplayInfo().getPixelFormat().getBitCount() + 7) / 8)];
    }

    /** Draws an image at the given coordinates */
    public void drawImage(int x, int y, int width, int height, int[] rgb888pixels) {
        synchronized (lock) {
            int xMin = Math.max(0, x);
            int yMin = Math.max(0, y);
            int xMax = Math.min(x + width, displayWidth);
            int yMax = Math.min(y + height, displayHeight);
            if (xMax <= xMin || yMax <= yMin) {
                return;
            }
            for (int targetY = yMin; targetY < yMax; targetY++) {
                System.arraycopy(
                        rgb888pixels,
                        (targetY - y) * width + xMin - x,
                        displayBuffer,
                        pixelAddress(xMin, targetY),
                        xMax - xMin);
            }
            markModified(xMin, yMin, xMax, yMax);
        }
    }

    /** Forces an immediate transfer of the modified screen area */
    public void flush() {
        synchronized (lock) {
            if (modifiedXMin < Integer.MAX_VALUE) {
                transferBuffer(modifiedXMin, modifiedYMin, modifiedXMax, modifiedYMax);
                modifiedXMin = Integer.MAX_VALUE;
                modifiedYMin = Integer.MAX_VALUE;
                modifiedXMax = Integer.MIN_VALUE;
                modifiedYMax = Integer.MIN_VALUE;
            }
        }
    }

    /** Sets the pixel at the given coordinates to the given color */
    public void setPixel(int x, int y, int color) {
        synchronized (lock) {
            if (x < 0 || y < 0 || x >= displayWidth || y >= displayHeight) {
                return;
            }
            displayBuffer[pixelAddress(x, y)] = color;
            markModified(x, y, x + 1, y + 1);
        }
    }

    /**
     * Sets the maximum delay between graphics updates and the screen buffer transfer to the display driver.
     * Setting the value to 0 will send all data immediately. A negative value will require an explicit
     * call to flush for the transfer. The default value is 15;
     */
    public void setTransferDelayMillis(int millis) {
        this.transferDelayMillis = millis;
    }

    /** Marks the given screen area as modified */
    private void markModified(int xMin, int yMin, int xMax, int yMax) {
        synchronized (lock) {
            modifiedXMin = Math.min(modifiedXMin, xMin);
            modifiedYMin = Math.min(modifiedYMin, yMin);
            modifiedXMax = Math.max(modifiedXMax, xMax);
            modifiedYMax = Math.max(modifiedYMax, yMax);
            if (transferDelayMillis == 0) {
                flush();
            } else if (pendingUpdate == null && transferDelayMillis > 0) {
                pendingUpdate = new TimerTask() {
                    @Override
                    public void run() {
                        pendingUpdate = null;
                        flush();
                    }};
                timer.schedule(pendingUpdate, transferDelayMillis);
            }
        }
    }

    // Private methods. Note that internally
    // - we assume coordinates are in range while we account for out-of-bounds coordinates in user methods.
    // - we use min/max coordinate bounds instead of width/height as in user methods.

    /** Returns the address of the given pixel in the display buffer */
    private int pixelAddress(int x, int y) {
        return y * driver.getDisplayInfo().getWidth() + x;
    }

    /** Transfers the given display buffer area to the display driver */
    private void transferBuffer(int xMin, int yMin, int xMax, int yMax) {
        synchronized (lock) {
            int xGranularity = driver.getDisplayInfo().getXGranularity();
            xMin = (xMin / xGranularity) * xGranularity;
            xMax = ((xMax + xGranularity - 1) / xGranularity) * xGranularity;

            int width = xMax - xMin;
            int height = yMax - yMin;

            PixelFormat pixelFormat = driver.getDisplayInfo().getPixelFormat();
            int bitsPerRow = width * pixelFormat.getBitCount();
            int bitOffset = 0;
            for (int i = 0; i < height; i++) {
                bitOffset += pixelFormat.writeRgb(
                        displayBuffer,
                        pixelAddress(xMin, yMin + i),
                        transferBuffer,
                        bitOffset,
                        width);
                // Transfer if the last row is reached or the next row would overflow the buffer.
                if (i == height - 1 || bitOffset + bitsPerRow > transferBuffer.length * 8) {
                    int rows = bitOffset / bitsPerRow;
                    driver.setPixels(xMin, yMin + i + 1 - rows, width, rows, transferBuffer);
                    bitOffset = 0;
                }
            }
        }
    }

    public void fillRect(int x, int y, int width, int height, int rgb888) {
        synchronized (lock) {
            int xMin = Math.max(0, x);
            int yMin = Math.max(0, y);
            int xMax = Math.min(x + width, displayWidth);
            int yMax = Math.min(y + height, displayHeight);
            if (xMax <= xMin || yMax <= yMin) {
                return;
            }
            for (int targetY = yMin; targetY < yMax; targetY++) {
                int start = pixelAddress(xMin, targetY);
                Arrays.fill(displayBuffer, start, start + xMax - xMin, rgb888);
            }
            markModified(xMin, yMin, xMax, yMax);
        }
    }

}
