package com.pi4j.driver;

import com.pi4j.util.Console;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        // Create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate stdin/stdout code)
        final var console = new Console();

        // Print program title/header
        console.title("<-- The Pi4J Project -->", "Hello Pi4J Driver World");
    }

    public static String test() {
        return "Test called at " + LocalDateTime.now();
    }
}
