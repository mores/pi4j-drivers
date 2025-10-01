package com.pi4j.drivers.hat.rasperry;

import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.GraphicsDisplay;
import com.pi4j.drivers.display.graphics.GraphicsDisplayDriver;
import com.pi4j.drivers.display.graphics.framebuffer.FramebufferDriver;
import com.pi4j.drivers.input.GameController;
import com.pi4j.drivers.input.linux.LinuxInputDriver;
import com.pi4j.io.ListenableOnOffRead;

public class SenseHat {
    private final Context pi4j;
    private GameController controller;
    private GraphicsDisplayDriver displayDriver;
    private GraphicsDisplay display;
    private LinuxInputDriver inputDriver;

    private final ListenableOnOffRead.Impl up = new ListenableOnOffRead.Impl();
    private final ListenableOnOffRead.Impl down = new ListenableOnOffRead.Impl();
    private final ListenableOnOffRead.Impl left = new ListenableOnOffRead.Impl();
    private final ListenableOnOffRead.Impl right = new ListenableOnOffRead.Impl();
    private final ListenableOnOffRead.Impl center = new ListenableOnOffRead.Impl();

    public SenseHat(Context pi4j) {
        this.pi4j = pi4j;
    }

    public LinuxInputDriver getInputDriver() {
        if (inputDriver == null) {
            inputDriver = LinuxInputDriver.forSenseHat();
        }
        return inputDriver;
    }

    public GameController getController() {
        if (controller == null) {
            LinuxInputDriver inputDriver = getInputDriver();
            inputDriver.addListener(this::handleEvent);
            controller = new GameController.Builder(pi4j)
                    .addDigitalInput(GameController.Key.DOWN, down)
                    .addDigitalInput(GameController.Key.LEFT, left)
                    .addDigitalInput(GameController.Key.RIGHT, right)
                    .addDigitalInput(GameController.Key.UP, up)
                    .addDigitalInput(GameController.Key.CENTER, center)
                    .build();
        }
        return controller;
    }


    public GraphicsDisplayDriver getDisplayDriver() {
        if (displayDriver == null) {
            displayDriver = FramebufferDriver.forSenseHat();
        }
        return displayDriver;
    }

    public GraphicsDisplay getDisplay() {
        if (display == null) {
            display = new GraphicsDisplay(getDisplayDriver());
        }
        return display;
    }

    private void handleEvent(LinuxInputDriver.Event event) {
        if (event.getType() != LinuxInputDriver.EV_KEY) {
            return;
        }
        Boolean state = switch (event.getValue()) {
            case LinuxInputDriver.STATE_PRESS -> true;
            case LinuxInputDriver.STATE_RELEASE -> false;
            default -> null;
        };
        if (state == null) {
            return;
        }
        switch (event.getCode()) {
            case LinuxInputDriver.KEY_DOWN -> down.setState(state);
            case LinuxInputDriver.KEY_UP -> up.setState(state);
            case LinuxInputDriver.KEY_LEFT -> left.setState(state);
            case LinuxInputDriver.KEY_RIGHT -> right.setState(state);
            case LinuxInputDriver.KEY_ENTER -> center.setState(state);
        }
    }
}
