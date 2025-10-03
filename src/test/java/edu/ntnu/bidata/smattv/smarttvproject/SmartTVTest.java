package edu.ntnu.bidata.smattv.smarttvproject;

import edu.ntnu.bidata.smarttv.smarttvproject.SmartTV;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SmartTV} class.
 * <p>
 * Verifies the behavior of power, channel operations, and exception handling.
 * </p>
 */
class SmartTVTest {

    /**
     * Tests turning the TV on and off.
     */
    @Test
    void turnOnAndOffTest() {
        SmartTV tv = new SmartTV(5);
        assertFalse(tv.isOn());
        tv.turnOn();
        assertTrue(tv.isOn());
        tv.turnOff();
        assertFalse(tv.isOn());
    }

    /**
     * Tests channel operations: set, up, and down.
     */
    @Test
    void channeOperationsTest() {
        SmartTV tv = new SmartTV(5);
        tv.turnOn();
        assertEquals(1, tv.getChannel());

        tv.setChannel(3);
        assertEquals(3, tv.getChannel());

        tv.channelUp();
        assertEquals(4, tv.getChannel());

        tv.channelDown();
        assertEquals(3, tv.getChannel());
    }

    /**
     * Tests handling of invalid channel numbers.
     */
    @Test
    void invalidChannelTest() {
        SmartTV tv = new SmartTV(5);
        tv.turnOn();
        assertThrows(IllegalArgumentException.class, () -> tv.setChannel(-1));
        assertThrows(IllegalArgumentException.class, () -> tv.setChannel(0));
        assertThrows(IllegalArgumentException.class, () -> tv.setChannel(6));
    }

    /**
     * Tests exception handling when operating on a TV that is off.
     */
    @Test
    void offStateExceptionTest() {
        SmartTV tv = new SmartTV(5);
        assertThrows(IllegalStateException.class, tv::getChannel);
        assertThrows(IllegalStateException.class, () -> tv.setChannel(1));
    }
}
