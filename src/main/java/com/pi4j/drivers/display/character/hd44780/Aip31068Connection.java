package com.pi4j.drivers.display.character.hd44780;

import com.pi4j.io.i2c.I2C;

/**
 * Implements the I2C protocol used for AIP31078 display controllers.
 * <p>
 * https://www.orientdisplay.com/wp-content/uploads/2022/08/AIP31068L.pdf, page 11.
 */
public class Aip31068Connection extends AbstractConnection {
    private final I2C i2c;

    public Aip31068Connection(I2C i2c) {
        this.i2c = i2c;
    }

    @Override
    protected boolean is8Bit() {
        return true;
    }

    @Override
    protected void setBacklight(boolean on) {
        // Unsupported
    }

    @Override
    protected void sendValue(Mode mode, int value) {
        i2c.write((byte) (mode == Mode.DATA ? 0x40 : 0x00),  (byte) value);
    }
}
