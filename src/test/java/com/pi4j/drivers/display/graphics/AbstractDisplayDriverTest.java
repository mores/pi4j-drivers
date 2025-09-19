package com.pi4j.drivers.display.graphics;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.st7789.St7789Driver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

public abstract class AbstractDisplayDriverTest {
    private Context pi4j;

    @BeforeEach
    public void setUp() {
        pi4j = Pi4J.newAutoContext();
    }

    @AfterEach
    public void tearDown() {
        pi4j.shutdown();
    }

    public abstract DisplayDriver createDriver(Context pi4j);

    @Test
    public void testFillRect() throws InterruptedException {
        DisplayDriver driver = createDriver(pi4j);
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
            Thread.sleep(100);
        }
        display.fillRect(0, 0, width, height, 0);
    }

}
