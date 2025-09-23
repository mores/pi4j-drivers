package com.pi4j.drivers.display.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
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

        BitmapFont font = BitmapFont.getLcdFont();

        int textWidth = font.renderText(display, 10, 20, "Hello Pi4J Monospaced", 0xff8888);
        assertEquals("Hello Pi4J Monospaced".length() * 6, textWidth);

        font.renderText(display, 10, 42, "Hello Pi4J 2x", 0x88ff88, EnumSet.of(BitmapFont.Option.PROPORTIONAL), 2, 2);
        font.renderText(display, 10, 75, "Hello Pi4J 3x", 0x8888ff, EnumSet.of(BitmapFont.Option.PROPORTIONAL), 3, 3);
        font.renderText(display, 10, 120, "Hello Pi4J", 0xffff00, EnumSet.of(BitmapFont.Option.PROPORTIONAL), 4, 4);

        display.flush(); // Make sure we don't get writes later.
    }

    @Test
    public void testSetPixel() throws InterruptedException {
        GraphicsDisplayDriver driver = createDriver(pi4j);
        GraphicsDisplay display = new GraphicsDisplay(driver);
        GraphicsDisplayInfo displayInfo = driver.getDisplayInfo();
        int width = displayInfo.getWidth();
        int height = displayInfo.getHeight();
        display.fillRect(0, 0, width, height, java.awt.Color.WHITE.getRGB() );
        Thread.sleep(100);

        for( int x = 0; x < width; x++ ) {
            for( int y = 0; y < height; y++ ) {
                display.setPixel( x, y, java.awt.Color.BLACK.getRGB() );
                Thread.sleep(1);
            }
        }

        display.flush(); // Make sure we don't get writes later.
    }
}
