package com.pi4j.drivers.display;

public enum PixelFormat {

    RGB_444( 4, 4, 4), // 12-bit color format with 4 bits for each color channel (red, green, blue)
    RGB_565( 5, 6, 5); // 16-bit color format that uses 5 bits for red, 6 bits for green, and 5 bits for blue

    private final int redBitCount;
    private final int greenBitCount;
    private final int blueBitCount;

    private final int redMask;
    private final int greenMask;
    private final int blueMask;

    PixelFormat(int redBitCount, int greenBitCount, int blueBitCount) {
        this.redBitCount = redBitCount;
        this.greenBitCount = greenBitCount;
        this.blueBitCount = blueBitCount;

        this.redMask = (1 << redBitCount) - 1;
        this.greenMask = (1 << greenBitCount) - 1;
        this.blueMask = (1 << blueBitCount) - 1;
    }

    // The total number of bits used by this format.
    int getBitCount() {
        return redBitCount + greenBitCount + blueBitCount;
    }

    /**
     * Writes count bits (up to 24) into the given buffer at the given bit offset.
     *
     * @param value The value to write.
     * @param count The number of bits (up to 24).
     * @param buffer The buffer to write the bits to
     * @param bitOffset The bit offset in the buffer
     */
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

    /** Converts a value from a 24 bit RGB integer value to "this" pixel format. */
    int fromRgb(int rgb) {
        int red = (rgb >> (24 - redBitCount)) & redMask;
        int green = (rgb >> (16 - greenBitCount)) & greenMask;
        int blue = (rgb >> (8 - blueBitCount)) & blueMask;
        return (red << (greenBitCount + blueBitCount)) | (green << (blueBitCount)) | blue;
    }

    /**
     * Writes a 24-bit RGB value into the given buffer in "this" pixel format at the given *bit* offset,
     * returning the number of bits written.
     */
    int writeRgb(int rgb, byte[] buffer, int bitOffset) {
        int count = redBitCount + greenBitCount + blueBitCount;
        writeBits(fromRgb(rgb), count, buffer, bitOffset);
        return count;
    }

    /**
     * Writes 24 bit integer RGB values from srcRgb to dst in "this" pixel format.
     *
     * @param srcRgb The source array with rgb values in 24 bit integers.
     * @param srcOffset The start offset in the soruce array
     * @param dst The destination buffer.
     * @param dstBitOffset The bit offset in the desitination buffer.
     * @param pixelCount The number of pixels to be transferred.
     * @return The number of bits written.
     */
    int writeRgb(int[] srcRgb, int srcOffset, byte[] dst, int dstBitOffset, int pixelCount) {
        int bitsWritten = 0;
        for (int i = 0; i < pixelCount; i++) {
            bitsWritten += writeRgb(srcRgb[srcOffset + i], dst, dstBitOffset + bitsWritten);
        }
        return bitsWritten;
    }

    /**
     * Fills the dst array with pixels of the same 24 bit rgb color, converted to "this" format.
     */
    int fillRgb(byte[] dst, int dstBitOffset, int pixelCount, int rgb) {
        int bitsWritten = 0;
        int nativeColor = fromRgb(rgb);
        int bitCount = getBitCount();
        for (int i = 0; i < pixelCount; i++) {
            writeBits(nativeColor, bitCount, dst, dstBitOffset + bitsWritten);
            bitsWritten += bitCount;
        }
        return bitsWritten;
    }
}
