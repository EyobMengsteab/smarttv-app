package edu.ntnu.bidata.smattv.smarttvproject;

import edu.ntnu.bidata.smarttv.smarttvproject.ProtocolHandler;
import edu.ntnu.bidata.smarttv.smarttvproject.SmartTV;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link ProtocolHandler} class.
 * <p>
 * Verifies command handling for power, channel operations, and error responses.
 * </p>
 */
class ProtocolHandlerTest {
    private final SmartTV tv = new SmartTV(5);
    private final ProtocolHandler handler = new ProtocolHandler();

    /**
     * Tests handling of TURN_ON and TURN_OFF commands.
     */
    @Test
    void turnOnAndOffTest() {
        assertEquals("OK ON", handler.handleCommand("TURN_ON", tv));
        assertEquals("OK OFF", handler.handleCommand("TURN_OFF", tv));
    }

    /**
     * Tests setting and getting the channel.
     */
    @Test
    void channelFlowTest() {
        handler.handleCommand("TURN_ON", tv);
        assertTrue(handler.handleCommand("SET_CHANNEL 2", tv).startsWith("OK"));
        assertTrue(handler.handleCommand("GET_CHANNEL", tv).contains("2"));
    }

    /**
     * Tests handling of invalid and malformed commands.
     */
    @Test
    void invalidCommandsTest() {
        assertTrue(handler.handleCommand("Goalllll", tv).startsWith("ERROR"));
        assertTrue(handler.handleCommand("SET_CHANNEL", tv).startsWith("ERROR"));
    }

    /**
     * Tests channel up and down commands.
     */
    @Test
    void channelUpDownTest() {
        handler.handleCommand("TURN_ON", tv);
        handler.handleCommand("SET_CHANNEL 2", tv);
        assertTrue(handler.handleCommand("CHANNEL_UP", tv).contains("3"));
        assertTrue(handler.handleCommand("CHANNEL_DOWN", tv).contains("2"));
    }
}
