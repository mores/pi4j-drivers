package com.pi4j.driver.display;

public class BaseGraphicsDisplayComponent {

    protected final GraphicsDisplayDriver driver;

    public BaseGraphicsDisplayComponent(GraphicsDisplayDriver driver) {
        this.driver = driver;
    }

    public void fillRect(int x, int y, int width, int height, int rgb888) {

        int[] values = new int[width * height];

        int red = (rgb888 >> 16) & 0xFF;
        int green = (rgb888 >> 8) & 0xFF;
        int blue = (rgb888) & 0xFF;

        int adjustedForColorDepth = 0;

        if (PixelFormat.RGB_444 == driver.getDisplayInfo().getPixelFormat()) {
            adjustedForColorDepth = rgb888toRgb444(red, green, blue);
        } else if (PixelFormat.RGB_565 == driver.getDisplayInfo().getPixelFormat()) {
            adjustedForColorDepth = rgb888toRgb565(red, green, blue);
        }

        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {

                final int index = ((col * width) + row);
                values[index] = adjustedForColorDepth;
            }
        }

        if (PixelFormat.RGB_444 == driver.getDisplayInfo().getPixelFormat()) {

            byte[] data = pack12(values);
            driver.setPixels(x, y, width, height, data);

        } else if (PixelFormat.RGB_565 == driver.getDisplayInfo().getPixelFormat()) {

            byte[] data = new byte[width * height * 16 / 8];

            for (int index = 0; index < values.length; index++) {
                data[2 * index] = (byte) (values[index] >> 8);
                data[2 * index + 1] = (byte) values[index];
            }

            driver.setPixels(x, y, width, height, data);
        }
    }

    protected byte[] pack12(int[] values) {
        int n = values.length;
        byte[] packed = new byte[(n * 12 + 7) / 8];

        int out = 0;
        for (int i = 0; i < n; i += 2) {
            int v1 = values[i] & 0xFFF; // 12 bits
            int v2 = (i + 1 < n) ? values[i + 1] & 0xFFF : 0;

            // Layout (24 bits):
            // byte0 = v1[11:4]
            // byte1 = v1[3:0] << 4 | v2[11:8]
            // byte2 = v2[7:0]

            packed[out++] = (byte) (v1 >>> 4);
            packed[out++] = (byte) ((v1 & 0xF) << 4 | (v2 >>> 8));
            packed[out++] = (byte) (v2 & 0xFF);
        }

        return packed;
    }

    protected int rgb888toRgb565(int r, int g, int b) {

        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("Invalid Colour (" + r + "," + g + "," + b + ")");
        }

        // rounding correction when reducing from 8 bits per channel to fewer bits, so that colors don’t just get
        // truncated but instead rounded
        // for red and blue: if the 3rd least significant bit is set (0x04), it adds 4.
        // For green: if the 2nd least significant bit is set (0x02), it adds 2
        if ((r & 0x04) != 0) {
            r += 0x04;

            if (r > 255) {
                r = 255;
            }
        }

        if ((g & 0x02) != 0) {
            g += 0x02;

            if (g > 255) {
                g = 255;
            }
        }

        if ((b & 0x04) != 0) {
            b += 0x04;

            if (b > 255) {
                b = 255;
            }
        }

        final int value = ((r >> 3) << 11) | ((g >> 2) << 5) | (b >> 3);
        return value;

    }

    /*
     * @param r red (0-255)
     * @param g green (0-255)
     * @param b blue (0-255)
     * @return 12-bit packed RGB444 value
     */
    protected int rgb888toRgb444(int r, int g, int b) {

        // Reduce 8-bit channel to 4-bit channel (0-255 -> 0-15)
        int r4 = (r >> 4) & 0xF;
        int g4 = (g >> 4) & 0xF;
        int b4 = (b >> 4) & 0xF;

        // Pack into 12 bits: RRRR GGGG BBBB
        return (r4 << 8) | (g4 << 4) | b4;
    }
}
