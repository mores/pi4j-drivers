package com.pi4j.drivers.display;

public enum PixelFormat {

    RGB_444( 4, 4, 4), // 12-bit color format with 4 bits for each color channel (red, green, blue)
    RGB_565( 5, 6, 5); // 16-bit color format that uses 5 bits for red, 6 bits for green, and 5 bits for blue

    private final int redBits;
    private final int greenBits;
    private final int blueBits;

    PixelFormat(int redBits, int greenBits, int blueBits) {
        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;
    }

    // The total number of bits used by this format.
    int getBitCount() {
        return redBits + greenBits + blueBits;
    }

    /** Writes count bits (up to 24) into the given buffer at the given bit offset. */
    private void writeBits(int value, int count, byte[] buffer, int bitOffset) {
        int byteOffset = bitOffset / 8;
        bitOffset %= 8;
        int mask = ((1 << count) - 1) << (32 - count - bitOffset);

        value <<= (32 - count - bitOffset);
        while (mask != 0) {
            buffer[byteOffset] = (byte) ((buffer[byteOffset] & ~(mask >> 24)) | (value >> 24));
            byteOffset++;
            value <<= 8;
            mask <<= 8;
        }
    }

    /**
     * Writes a 24-bit RGB value into the given buffer in this pixel format at the given *bit* offset,
     * returning the number of bits written.
     */
    int writeRgb(int rgb, byte[] buffer, int bitOffset) {
        int red = (rgb >> (24 - redBits)) & 0xff;
        int green = (rgb >> (16 - greenBits)) & 0xff;
        int blue = (rgb >> (8 - blueBits)) & 0xff;
        int count = redBits + greenBits + blueBits;

        writeBits((red << (greenBits + blueBits)) | (green << (blueBits)) | blue, count, buffer, bitOffset);
        return count;
    }
}
