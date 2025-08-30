package com.pi4j.driver.display;

import java.io.IOException;

public interface GraphicsDisplayDriver {

    DisplayInfo getDisplayInfo();

    void setPixels(int x, int y, int width, int height, byte[] data) throws IOException;
}
