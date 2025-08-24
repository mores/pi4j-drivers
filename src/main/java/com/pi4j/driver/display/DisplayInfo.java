package com.pi4j.driver.display;

import java.util.List;

public interface DisplayInfo {
  int getWidth(); // In pixel
  int getHeight(); 
  List<ColorFormat> getColorFormat();
}
