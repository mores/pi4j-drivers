package com.pi4j.driver.display;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwtGraphicsDisplayComponent extends BaseGraphicsDisplayComponent {

    private static Logger log = LoggerFactory.getLogger(AwtGraphicsDisplayComponent.class);

    public AwtGraphicsDisplayComponent(GraphicsDisplayDriver driver) {
        super(driver);
    }

    public void display(BufferedImage rawImg) {

        log.debug("display: {} {} x {}", rawImg.getType(), rawImg.getWidth(), rawImg.getHeight());
        DisplayInfo displayInfo = driver.getDisplayInfo();

        // clip the image to what will fit on the display
        BufferedImage img = rawImg.getSubimage(0, 0, Math.min(rawImg.getWidth(), displayInfo.getWidth()),
                Math.min(rawImg.getHeight(), displayInfo.getHeight()));

        int[] values = new int[img.getWidth() * img.getHeight()];

        DataBuffer dataBuffer = img.getRaster().getDataBuffer();
        if (dataBuffer instanceof DataBufferByte) {

            byte[] pixels = ((DataBufferByte) dataBuffer).getData();

            boolean hasAlphaChannel = img.getAlphaRaster() != null;
            int pixelLength = 3;
            if (hasAlphaChannel) {
                pixelLength = 4;
            }

            for (int x = 0; x < img.getWidth(); x++) {

                for (int y = 0; y < img.getHeight(); y++) {

                    int pos = (y * pixelLength * img.getWidth()) + (x * pixelLength);

                    int alpha = 0;
                    int blue = 0;
                    int green = 0;
                    int red = 0;

                    if (BufferedImage.TYPE_3BYTE_BGR == img.getType()) {
                        blue = 0xff & pixels[pos++];
                        green = 0xff & pixels[pos++];
                        red = 0xff & pixels[pos++];
                    } else if (BufferedImage.TYPE_BYTE_GRAY == img.getType()) {
                        int grayPos = (y * img.getWidth()) + x;

                        blue = 0xff & pixels[grayPos];
                        green = 0xff & pixels[grayPos];
                        red = 0xff & pixels[grayPos];

                    } else {
                        alpha = 0xff & pixels[pos++];

                        blue = 0xff & pixels[pos++];
                        green = 0xff & pixels[pos++];
                        red = 0xff & pixels[pos++];
                    }

                    if (x < displayInfo.getWidth() && y < displayInfo.getHeight()) {

                        final int index = (y * img.getWidth()) + x;

                        switch (displayInfo.getPixelFormat()) {
                            case RGB_444:
                                values[index] = rgb888toRgb444(red, green, blue);
                                break;
                            case RGB_565:
                                values[index] = rgb888toRgb565(red, green, blue);
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unsupported pixel format: " + displayInfo.getPixelFormat());
                        }

                    }
                }
            }
        } else if (dataBuffer instanceof DataBufferInt) {
            int[] pixels = ((DataBufferInt) dataBuffer).getData();

            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {

                    int i = x + y * img.getWidth();
                    int alpha = (pixels[i] >> 24) & 0xff;
                    int red = (pixels[i] >> 16) & 0xff;
                    int green = (pixels[i] >> 8) & 0xff;
                    int blue = (pixels[i] >> 0) & 0xff;

                    if (x < displayInfo.getWidth() && y < displayInfo.getHeight()) {
                        final int index = (y * img.getWidth()) + x;

                        switch (displayInfo.getPixelFormat()) {
                            case RGB_444:
                                values[index] = rgb888toRgb444(red, green, blue);
                                break;
                            case RGB_565:
                                values[index] = rgb888toRgb565(red, green, blue);
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unsupported pixel format: " + displayInfo.getPixelFormat());
                        }

                    }

                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported dataBufferType: " + dataBuffer.getClass());
        }

        if (PixelFormat.RGB_444 == driver.getDisplayInfo().getPixelFormat()) {

            byte[] data = pack12(values);
            driver.setPixels(0, 0, displayInfo.getWidth(), displayInfo.getHeight(), data);

        } else if (PixelFormat.RGB_565 == driver.getDisplayInfo().getPixelFormat()) {

            byte[] data = new byte[displayInfo.getWidth() * displayInfo.getWidth() * 16 / 8];

            for (int index = 0; index < values.length; index++) {
                data[2 * index] = (byte) (values[index] >> 8);
                data[2 * index + 1] = (byte) values[index];
            }

            driver.setPixels(0, 0, displayInfo.getWidth(), displayInfo.getHeight(), data);
        }

    }
}
