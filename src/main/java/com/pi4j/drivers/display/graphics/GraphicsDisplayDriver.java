package com.pi4j.drivers.display.graphics;

public interface GraphicsDisplayDriver {

    GraphicsDisplayInfo getDisplayInfo();

    void setPixels(int x, int y, int width, int height, byte[] data);
}
