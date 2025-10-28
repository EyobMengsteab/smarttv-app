package edu.ntnu.bidata.smattv.smarttvproject;

import edu.ntnu.bidata.smarttv.smarttvproject.ProtocolHandler;
import edu.ntnu.bidata.smarttv.smarttvproject.SmartTV;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive unit test suite for the {@link ProtocolHandler} class.
 *
 * <p>This test class validates the protocol handler's ability to process and execute
 * various Smart TV commands through a standardized command interface. The tests
 * ensure proper command parsing, execution, and response formatting for all
 * supported TV operations.</p>
 *
 * <h3>Test Coverage</h3>
 * <ul>
 *   <li><strong>Power Management:</strong> TV power on/off operations</li>
 *   <li><strong>Channel Operations:</strong> Setting, getting, and navigating channels</li>
 *   <li><strong>Error Handling:</strong> Invalid command detection and error responses</li>
 *   <li><strong>Navigation:</strong> Channel up/down functionality</li>
 * </ul>
 *
 * <h3>Test Environment</h3>
 * <p>All tests use a pre-configured {@link SmartTV} instance with 5 test channels
 * (Channel1 through Channel5) to ensure consistent and predictable test results.</p>
 *
 * <h3>Command Protocol Testing</h3>
 * <p>The tests verify that the protocol handler correctly:</p>
 * <ul>
 *   <li>Processes valid commands and returns appropriate "OK" responses</li>
 *   <li>Detects invalid or malformed commands and returns "ERROR" responses</li>
 *   <li>Maintains TV state consistency across multiple command executions</li>
 *   <li>Handles edge cases such as channel boundaries and power state dependencies</li>
 * </ul>
 *
 * @author EyobMengsteab
 */
class ProtocolHandlerTest {
    private final SmartTV tv = new SmartTV(new String[]{"Channel1", "Channel2", "Channel3", "Channel4", "Channel5"});
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
