package com.pi4j.driver.interfaces;

import java.awt.image.BufferedImage;

public interface Video {

	void display(BufferedImage img) throws Exception;

	void fill(int ledColor) throws Exception;

	void pixel(int x, int y, int ledColor) throws Exception;
}
