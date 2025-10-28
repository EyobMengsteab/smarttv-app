package edu.ntnu.bidata.smattv.smarttvproject;

import edu.ntnu.bidata.smarttv.smarttvproject.SmartTV;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit test suite for the {@link SmartTV} class.
 *
 * <p>This test class validates the core functionality of the Smart TV system,
 * ensuring proper state management, channel operations, and error handling
 * across various operational scenarios.</p>
 *
 * <h3>Test Coverage</h3>
 * <ul>
 *   <li><strong>Power Management:</strong> TV power on/off state transitions and validation</li>
 *   <li><strong>Channel Operations:</strong> Channel setting, navigation (up/down), and retrieval</li>
 *   <li><strong>Input Validation:</strong> Invalid channel number handling and boundary checks</li>
 *   <li><strong>State Management:</strong> Exception handling for operations on powered-off TV</li>
 * </ul>
 *
 * <h3>Test Environment</h3>
 * <p>All tests utilize a standardized Smart TV configuration with 5 predefined channels:
 * This setup ensures consistent test execution and predictable channel numbering (1-5).</p>
 *
 * <h3>Validation Scenarios</h3>
 * <p>The test suite verifies that the Smart TV:</p>
 * <ul>
 *   <li>Initializes in the OFF state by default</li>
 *   <li>Properly transitions between ON and OFF states</li>
 *   <li>Maintains correct channel state during navigation operations</li>
 *   <li>Enforces channel boundaries (1 to number of available channels)</li>
 *   <li>Throws appropriate exceptions for invalid operations and states</li>
 * </ul>
 *
 * <h3>Exception Testing</h3>
 * <p>Comprehensive exception handling validation ensures:</p>
 * <ul>
 *   <li>{@code IllegalArgumentException} for invalid channel numbers (≤0 or >available channels)</li>
 *   <li>{@code IllegalStateException} for channel operations when TV is powered off</li>
 * </ul>
 *
 * @author EyobMengsteab
 */
class SmartTVTest {

    /**
     * Tests turning the TV on and off.
     */
    @Test
    void turnOnAndOffTest() {
        SmartTV tv = new SmartTV(new String[]{"BBC", "CNN", "MTV", "Discovery", "History"});
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
        SmartTV tv = new SmartTV(new String[]{"BBC", "CNN", "MTV", "Discovery", "History"});
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
        SmartTV tv = new SmartTV(new String[]{"BBC", "CNN", "MTV", "Discovery", "History"});
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
        SmartTV tv = new SmartTV(new String[]{"BBC", "CNN", "MTV", "Discovery", "History"});
        assertThrows(IllegalStateException.class, tv::getChannel);
        assertThrows(IllegalStateException.class, () -> tv.setChannel(1));
    }
}
