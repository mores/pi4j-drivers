package com.pi4j.drivers.input.rotary.adafruit5880;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;

public class Adafruit5880Listener implements DigitalStateChangeListener {

    private static Logger log = LoggerFactory.getLogger(Adafruit5880Listener.class);

    private Adafruit5880Driver driver;

    private List<Consumer<Boolean>> buttonListeners;
    private List<IntConsumer> positionListeners;

    private int lastKnownPosition;
    private boolean lastKnownButtonState;

    public Adafruit5880Listener(Adafruit5880Driver driver) {
        this.driver = driver;
        this.buttonListeners = new ArrayList<>();
        this.positionListeners = new ArrayList<>();
    }

    public void addButtonListener(Consumer<Boolean> buttonListener) {
        this.buttonListeners.add(buttonListener);
    }

    public void addPositionListener(IntConsumer positionListener) {
        this.positionListeners.add(positionListener);
    }

    @Override
    public void onDigitalStateChange(DigitalStateChangeEvent event) {

        log.trace(">>> Enter: onDigitalStateChange: " + event);

        if (event.state() == DigitalState.LOW) {
            // Only way to clear the interupt is to read the position
            if (lastKnownPosition != driver.getPosition()) {
                lastKnownPosition = driver.getPosition();
                log.debug("Position changed: " + lastKnownPosition);

                for (IntConsumer positionListener : positionListeners) {
                    positionListener.accept(lastKnownPosition);
                }
            }

            if (lastKnownButtonState != driver.isPressed()) {
                lastKnownButtonState = driver.isPressed();
                log.debug("Button changed pressed: " + driver.isPressed());

                for (Consumer<Boolean> buttonListener : buttonListeners) {
                    buttonListener.accept(lastKnownButtonState);
                }
            }
        }
    }
}
