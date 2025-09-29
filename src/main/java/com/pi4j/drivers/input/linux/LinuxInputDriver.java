package com.pi4j.drivers.input.linux;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Driver for linux input devices.
 */
public class LinuxInputDriver implements Closeable {

    private static final String SENSE_STICK_NAME = "Raspberry Pi Sense HAT Joystick";

    public static final int EV_KEY = 1;

    public static final int STATE_RELEASE = 0;
    public static final int STATE_PRESS = 1;
    public static final int STATE_HOLD = 2;

    public static final int KEY_UP = 103;
    public static final int KEY_LEFT = 105;
    public static final int KEY_RIGHT = 106;
    public static final int KEY_DOWN = 108;
    public static final int KEY_ENTER = 28;

    private final Object lock = new Object(); // Gates the listener list.
    private final List<Consumer<Event>> listeners = new ArrayList<>();
    private final InputStream inputStream;
    private boolean closed;

    /**
     * Resolves a linux input device name to the corresponding device path in /dev/input. Returns null if not found.
     */
    public static String resolveInputName(String name) {
        File dir = new File("/sys/class/input/");
        for (File candidate :  dir.listFiles()) {
            if (candidate.getName().startsWith("event")) {
                File nameFile = new File(new File(candidate, "device"), "name");
                if (nameFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(nameFile))) {
                        String line = reader.readLine().trim();
                        if (line.equals(name)) {
                           return "/dev/input/" + candidate.getName();
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Creates a Pi4j linux input driver for the sense hat.
     */
    public static LinuxInputDriver forSenseHat() {
        String devicePath = resolveInputName(SENSE_STICK_NAME);
        if (devicePath == null) {
            throw new IllegalStateException("Sense hat not found");
        }
        return new LinuxInputDriver(devicePath);
    }

    /**
     * Creates a linux input driver for the given device path.
     */
    public LinuxInputDriver(String devicePath) {
        try {
            this.inputStream = new BufferedInputStream(new FileInputStream(devicePath));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(Event.STRUCT_SIZE);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        while (!closed && inputStream.readNBytes(buffer.array(), 0, Event.STRUCT_SIZE) == Event.STRUCT_SIZE) {
                            int type = buffer.getShort(Event.OFFSET_TYPE);
                            if (type != 0) {
                                long seconds = buffer.getLong(Event.OFFSET_SECONDS);
                                long microSeconds = buffer.getLong(Event.OFFSET_MICROSECONDS);
                                int code = buffer.getShort(Event.OFFSET_CODE);
                                int value = buffer.getInt(Event.OFFSET_VALUE);
                                Event event = new Event(Instant.ofEpochSecond(seconds, microSeconds * 1000), type, code, value);
                                notifyListeners(event);
                            }
                        }
                    } catch (IOException e) {
                        if (!closed) {
                            throw new com.pi4j.io.exception.IOException(e);
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            throw new com.pi4j.io.exception.IOException(e);
        }
    }

    public void addListener(Consumer<Event> listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void removeListener(Consumer<Event> listener) {
        synchronized (lock) {
            listeners.remove(listener);
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new com.pi4j.io.exception.IOException(e);
        }
    }

    private void notifyListeners(Event event) {
        synchronized (lock) {
            for (Consumer<Event> listener : this.listeners) {
                listener.accept(event);
            }
        }
    }


    public static class Event {
        private static final int STRUCT_SIZE = 24;
        private static final int OFFSET_SECONDS = 0;
        private static final int OFFSET_MICROSECONDS = 8;
        private static final int OFFSET_TYPE = 16;
        private static final int OFFSET_CODE = 18;
        private static final int OFFSET_VALUE = 20;

        private final Instant time;
        private final int type;
        private final int code;
        private final int value;

        Event(Instant time, int type, int code, int value) {
            this.time = time;
            this.type = type;
            this.code = code;
            this.value = value;
        }

        public Instant getTime() {
            return time;
        }
        public int getType() {
            return type;
        }
        public int getCode() {
            return code;
        }
        public int getValue() {
            return value;
        }
        public String toString() {
            return "Event time: " + time + ", type: " + type + ", code: " + code + ", value: " + value;
        }
    }
}
