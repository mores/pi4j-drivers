package com.pi4j.drivers.display.graphics.st7789;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.BaseDisplayComponent;
import com.pi4j.drivers.display.graphics.DisplayInfo;
import com.pi4j.drivers.display.graphics.PixelFormat;
import com.pi4j.exception.Pi4JException;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfigBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * This test assumes the waveshare 1.3inch IPS display HAT pinout, see https://www.waveshare.com/1.3inch-lcd-hat.htm
 */
public class St7789DriverTest {
    private static final int BACKLIGHT_ADDRESS = 24;
    private static final int DC_ADDRESS = 25;
    private static final int SPI_BAUDRATE = 62_500_000;
    private static final int RST_ADDRESS = 27;
    private static final int SPI_BUS = 0;
    private static final int SPI_ADDRESS = 0;

    private Context pi4j;

    @BeforeEach
    public void setUp() {
        pi4j = Pi4J.newAutoContext();
    }

    @AfterEach
    public void tearDown() {
        pi4j.shutdown();
    }

    @Test
    public void testFillRect() {
        St7789Driver driver = createDriver();
        BaseDisplayComponent display = new BaseDisplayComponent(driver);
        DisplayInfo displayInfo = driver.getDisplayInfo();
        int width = displayInfo.getWidth();
        int height = displayInfo.getHeight();
        display.fillRect(0, 0, width, height, 0xffffff);
        Random random = new Random(0);
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int w = random.nextInt(width - x);
            int h = random.nextInt(height - y);
            int color = random.nextInt(0xffffff);
            display.fillRect(x, y, w, h, color);
        }
    }

    private St7789Driver createDriver() {
        try {
            DigitalOutput bl = pi4j
                    .create(DigitalOutputConfigBuilder.newInstance(pi4j).address(BACKLIGHT_ADDRESS).build());
            bl.high();
            DigitalOutput rst = pi4j.create(DigitalOutputConfigBuilder.newInstance(pi4j).address(RST_ADDRESS).build());
            rst.high();
            DigitalOutput dc = pi4j.create(DigitalOutputConfigBuilder.newInstance(pi4j).address(DC_ADDRESS).build());
            Spi spi = pi4j.create(
                    SpiConfigBuilder.newInstance(pi4j).bus(SPI_BUS).address(SPI_ADDRESS).baud(SPI_BAUDRATE).build());
            return new St7789Driver(spi, dc, 240, PixelFormat.RGB_444);
        } catch (RuntimeException e) {
            // TODO(https://github.com/Pi4J/pi4j/issues/489): Catch Pi4j exceptions instead.
            Assumptions.abort("St7789 not found");
            throw new RuntimeException(e);
        }
    }

}
