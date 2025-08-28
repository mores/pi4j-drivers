package com.pi4j.driver.display;

import java.io.IOException;

public interface GraphicsDisplayDriver {
    DisplayInfo getDisplayInfo();

    void setPixels(byte[] data) throws IOException;
}
