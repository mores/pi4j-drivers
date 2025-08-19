package com.pi4j.driver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void test() {
        assertTrue(Main.test().startsWith("Test called at"));
    }
}
