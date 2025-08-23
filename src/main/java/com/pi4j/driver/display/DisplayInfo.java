package com.pi4j.driver.display;

public interface DisplayInfo {
  int getWidth(); // In pixel
  int getHeight(); 
  ColorFormat getColorFormat();
}
