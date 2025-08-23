package com.pi4j.driver.display;

public interface GraphicsDisplayDriver {
  DisplayInfo getDisplayInfo();

  /** 
   * Renders a horizontal line of “bulk” pixel data. 
   * If length < w*bytePerPixel, pixels will be repeated.
   * x, y, and x + w are expected to be within the bounds of the display.
   */
  void setPixels(int x, int y, int w, byte[] data, int offset, int length) throws Exception;
}
