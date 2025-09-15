package com.pi4j.drivers.display.graphics;

public interface DisplayDriver {

    DisplayInfo getDisplayInfo();

    void setPixels(int x, int y, int width, int height, byte[] data);
}
