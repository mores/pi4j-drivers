package com.pi4j.drivers.display.graphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BaseDisplayComponent {
    // TODO(https://github.com/Pi4J/pi4j/issues/475): Remove or update this limitation.
    private static final int MAX_TRANSFER_SIZE = 4000;

    protected final DisplayDriver driver;
    private final Object lock = new Object();
    private final int[] displayBuffer;
    private final byte[] transferBuffer;
    private final Timer timer = new Timer();

    private int modifiedXMax = Integer.MIN_VALUE;
    private int modifiedXMin = Integer.MAX_VALUE;
    private int modifiedYMax = Integer.MIN_VALUE;
    private int modifiedYMin = Integer.MAX_VALUE;
    private boolean updatePending = false;
    private int transferDelayMillis = 20;

    public BaseDisplayComponent(DisplayDriver driver) {
        this.driver = driver;
        displayBuffer = new int[driver.getDisplayInfo().getWidth() * driver.getDisplayInfo().getHeight()];
        transferBuffer = new byte[Math.min(
                MAX_TRANSFER_SIZE,
                (driver.getDisplayInfo().getHeight() * driver.getDisplayInfo().getWidth()
                        * driver.getDisplayInfo().getPixelFormat().getBitCount() + 7) / 8)];
    }

    // it is possible that rgb888pixels contains more than will fit
    // we will just ignore them
    public void drawImage(int x, int y, int width, int height, int[] rgb888pixels) {
        synchronized (lock) {
            for (int i = 0; i < height; i++) {
                System.arraycopy(
                        rgb888pixels,
                        i * width,
                        displayBuffer,
                        pixelAddress(x, y + i),
                        width);
            }
            markDirty(x, y, width, height);
        }
    }

    public void setPixel(int x, int y, int color) {
        synchronized (lock) {
            displayBuffer[pixelAddress(x, y)] = color;
            markDirty(x, y, 1, 1);
        }
    }


    private void markDirty(int x, int y, int width, int height) {
        synchronized (lock) {
            if (transferDelayMillis == 0) {
                transferBuffer(x, y, width, height);
            } else {
                if (x < modifiedXMin) {
                    modifiedXMin = x;
                }
                if (x + width > modifiedXMax) {
                    modifiedXMax = x + width;
                }
                if (y < modifiedYMin) {
                    modifiedYMin = y;
                }
                if (y + height > modifiedYMax) {
                    modifiedYMax = y + height;
                }
                if (!updatePending) {
                    updatePending = true;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                updatePending = false;
                                transferBuffer(
                                        modifiedXMin,
                                        modifiedYMin,
                                        modifiedXMax - modifiedXMin,
                                        modifiedYMax - modifiedYMin);
                            }
                        }
                    }, transferDelayMillis);
                }
            }
        }
    }

    /** Returns the address of the given pixel in the display buffer */
    private int pixelAddress(int x, int y) {
        return y * driver.getDisplayInfo().getWidth() + x;
    }

    private void transferBuffer(int x, int y, int width, int height) {
        synchronized (lock) {
            PixelFormat pixelFormat = driver.getDisplayInfo().getPixelFormat();
            int bitsPerRow = width * pixelFormat.getBitCount();
            int bitOffset = 0;
            for (int i = 0; i < height; i++) {
                bitOffset += pixelFormat.writeRgb(
                        displayBuffer,
                        pixelAddress(x, y + i),
                        transferBuffer,
                        bitOffset,
                        width);
                // Transfer if the last row is reached or the next row would overflow the buffer.
                if (i == height - 1 || bitOffset + bitsPerRow > transferBuffer.length * 8) {
                    int rows = bitOffset / bitsPerRow;
                    driver.setPixels(x, y + i + 1 - rows, width, rows, transferBuffer);
                    bitOffset = 0;
                }
            }
        }
    }

    public void fillRect(int x, int y, int width, int height, int rgb888) {
        synchronized (lock) {
            if (width <= 0 || height <= 0) {
                return;
            }
            for (int i = 0; i < height; i++) {
                int start = pixelAddress(x, y + i);
                Arrays.fill(displayBuffer, rgb888, start, start + width);
            }
            markDirty(x, y, width, height);
        }
    }
}
