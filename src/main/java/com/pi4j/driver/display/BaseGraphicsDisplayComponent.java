package com.pi4j.driver.display;

public class BaseGraphicsDisplayComponent {

    protected final GraphicsDisplayDriver driver;

    public BaseGraphicsDisplayComponent(GraphicsDisplayDriver driver) {
        this.driver = driver;
    }

    public void fillRect(int x, int y, int width, int height, int rgb888) throws java.io.IOException {

        byte[] data = new byte[width * height * 16 / 8];

        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {

                int red = (rgb888 >> 16) & 0xFF;
                int green = (rgb888 >> 8) & 0xFF;
                int blue = (rgb888) & 0xFF;

                final int index = ((col * width) + row) * 2;

                final int value = rgb888toRgb565(red, green, blue);

                data[index] = (byte) (value >> 8);
                data[index + 1] = (byte) value;
            }
        }

        driver.setPixels(x, y, width, height, data);
    }

    private int rgb888toRgb565(int r, int g, int b) {

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

}
