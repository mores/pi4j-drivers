package com.pi4j.drivers.display.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.BitmapFont;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

public abstract class AbstractGraphicsDisplayDriverTest {
    private Context pi4j;

    @BeforeEach
    public void setUp() {
        pi4j = Pi4J.newAutoContext();
    }

    @AfterEach
    public void tearDown() {
        pi4j.shutdown();
    }

    public abstract GraphicsDisplayDriver createDriver(Context pi4j);

    @Test
    public void testFillRect() throws InterruptedException {
        GraphicsDisplayDriver driver = createDriver(pi4j);
        GraphicsDisplay display = new GraphicsDisplay(driver);
        GraphicsDisplayInfo displayInfo = driver.getDisplayInfo();
        int width = displayInfo.getWidth();
        int height = displayInfo.getHeight();
        display.fillRect(0, 0, width, height, 0x0);
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
        display.flush(); // Make sure we don't get writes later.
    }

    @Test
    public void testBitmapFont() throws InterruptedException {
        GraphicsDisplayDriver driver = createDriver(pi4j);
        GraphicsDisplay display = new GraphicsDisplay(driver);
        GraphicsDisplayInfo displayInfo = driver.getDisplayInfo();
        int width = displayInfo.getWidth();
        int height = displayInfo.getHeight();
        display.fillRect(0, 0, width, height, 0);

        BitmapFont font = BitmapFont.get5x8Font();
        BitmapFont proportionalFont = BitmapFont.get5x8Font(BitmapFont.Option.PROPORTIONAL);

        int textWidth = display.renderText(10, 20, "Hello Pi4J Monospaced", font, 0xff8888);
        assertEquals("Hello Pi4J Monospaced".length() * 6, textWidth);

        display.renderText(10, 42, "Hello Pi4J 2x", proportionalFont, 0x88ff88, 2, 2);
        display.renderText(10, 75, "Hello Pi4J 3x", proportionalFont, 0x8888ff, 3, 3);
        display.renderText(10, 120, "Hello Pi4J", proportionalFont, 0xffff00, 4, 4);

        display.flush(); // Make sure we don't get writes later.
    }
}
