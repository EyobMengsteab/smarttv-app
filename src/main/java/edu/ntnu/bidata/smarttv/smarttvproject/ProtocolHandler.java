package edu.ntnu.bidata.smarttv.smarttvproject;

/**
 * Handles remote control protocol commands for a {@link SmartTV} instance.
 * <p>
 * The {@code ProtocolHandler} class serves as a command interpreter and executor for
 * Smart TV remote control operations. It processes text-based protocol commands from
 * network clients and translates them into appropriate method calls on the target
 * {@link SmartTV} instance, providing standardized responses for both successful
 * operations and error conditions.
 * </p>
 *
 * <p>
 * This class implements a stateless command processing pattern, making it thread-safe
 * for concurrent use across multiple client connections. Each command is processed
 * independently without maintaining any internal state between invocations.
 * </p>
 *
 * <p>
 * <strong>Supported Protocol Commands:</strong>
 * <ul>
 *   <li><strong>Power Control:</strong> TURN_ON, TURN_OFF, GET_STATE</li>
 *   <li><strong>Channel Operations:</strong> SET_CHANNEL [number], CHANNEL_UP, CHANNEL_DOWN, GET_CHANNEL</li>
 *   <li><strong>Information Queries:</strong> GET_CHANNELS</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Response Format:</strong> All responses follow a consistent format:
 * <ul>
 *   <li><strong>Success:</strong> "OK [result]" where result varies by command</li>
 *   <li><strong>Error:</strong> "ERROR: [description]" with descriptive error message</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Error Handling:</strong> The handler gracefully manages various error conditions
 * including invalid commands, malformed input, TV state violations, and unexpected exceptions,
 * always returning a well-formed error response rather than throwing exceptions.
 * </p>
 *
 * @author EyobMengsteab
 */
public class ProtocolHandler {

    /**
     * Processes a command string and applies it to the given {@link SmartTV}.
     * <p>
     * This method serves as the main entry point for protocol command processing.
     * It parses the input command, validates the syntax, executes the appropriate
     * operation on the Smart TV, and returns a standardized response. The method
     * handles all error conditions gracefully and never throws exceptions.
     * </p>
     * <p>
     * <strong>Command Processing Flow:</strong>
     * <ol>
     *   <li>Input validation and null/blank checking</li>
     *   <li>Command parsing and tokenization</li>
     *   <li>Command type identification (case-insensitive)</li>
     *   <li>Parameter validation for parameterized commands</li>
     *   <li>Smart TV operation execution</li>
     *   <li>Response formatting and return</li>
     * </ol>
     * </p>
     *
     * <p>
     * <strong>Supported Commands:</strong>
     * <ul>
     *   <li><strong>TURN_ON</strong> - Powers on the TV
     *       <br>Response: "OK ON"</li>
     *   <li><strong>TURN_OFF</strong> - Powers off the TV
     *       <br>Response: "OK OFF"</li>
     *   <li><strong>GET_STATE</strong> - Retrieves current power state
     *       <br>Response: "OK ON" or "OK OFF"</li>
     *   <li><strong>GET_CHANNEL</strong> - Retrieves current channel number
     *       <br>Response: "OK [number]"</li>
     *   <li><strong>GET_CHANNELS</strong> - Retrieves all available channel names
     *       <br>Response: "OK [name1,name2,name3,...]"</li>
     *   <li><strong>SET_CHANNEL [number]</strong> - Sets TV to specific channel
     *       <br>Response: "OK [number]"</li>
     *   <li><strong>CHANNEL_UP</strong> - Increments channel by one
     *       <br>Response: "OK [new_channel_number]"</li>
     *   <li><strong>CHANNEL_DOWN</strong> - Decrements channel by one
     *       <br>Response: "OK [new_channel_number]"</li>
     * </ul>
     * </p>
     *
     * <p>
     * <strong>Error Conditions:</strong>
     * <ul>
     *   <li><strong>Empty/null input:</strong> "ERROR: Empty command"</li>
     *   <li><strong>Unknown command:</strong> "ERROR: Unknown Command"</li>
     *   <li><strong>Missing parameters:</strong> "ERROR: Missing channel number"</li>
     *   <li><strong>TV state violations:</strong> "ERROR: TV is OFF" (when accessing channels while TV is off)</li>
     *   <li><strong>Invalid parameters:</strong> "ERROR: Invalid channel number"</li>
     *   <li><strong>Format errors:</strong> "ERROR: Invalid command format"</li>
     * </ul>
     * </p>
     *
     * <p>
     * <strong>Thread Safety:</strong> This method is thread-safe as it operates on the
     * provided Smart TV instance without maintaining any internal state. Multiple
     * threads can safely call this method concurrently with different or the same
     * Smart TV instances.
     * </p>
     *
     * <p>
     * <strong>Input Format:</strong> Commands are case-insensitive and parameters
     * are separated by spaces. Leading and trailing whitespace is automatically
     * trimmed. For example: "  turn_on  ", "TURN_ON", and "Turn_On" are all equivalent.
     * </p>
     *
     * @param input the command string from the client. May contain leading/trailing whitespace.
     *              Commands are case-insensitive and parameters should be space-separated.
     *              Examples: "TURN_ON", "SET_CHANNEL 3", "get_channels"
     * @param tv    the {@link SmartTV} instance to control. Must not be {@code null}.
     *              All operations will be performed on this TV instance
     * @return the response string for the command following the protocol format.
     *         Success responses start with "OK" followed by relevant data.
     *         Error responses start with "ERROR:" followed by a descriptive message.
     *         Never returns {@code null}
     * @throws NullPointerException if {@code tv} parameter is {@code null}
     */
    public String handleCommand(String input, SmartTV tv) {
        if (input == null || input.isBlank()) return "ERROR: Empty command";

        String[] parts = input.trim().split(" ");
        String cmd = parts[0].toUpperCase();

        try {
            switch (cmd) {
                case "TURN_ON":
                    tv.turnOn();
                    return "OK ON";
                case "TURN_OFF":
                    tv.turnOff();
                    return "OK OFF";
                case "GET_STATE":
                    return "OK " + (tv.isOn() ? "ON" : "OFF");
                case "GET_CHANNEL":
                    return "OK " + tv.getChannel();
                case "GET_CHANNELS":
                    String[] names = tv.getChannelNames();
                    return "OK " + String.join(",", names);
                case "SET_CHANNEL":
                    if (parts.length < 2)
                        return "ERROR: Missing channel number";
                    int ch = Integer.parseInt(parts[1]);
                    tv.setChannel(ch);
                    return "OK " + ch;
                case "CHANNEL_UP":
                    return "OK " + tv.channelUp();
                case "CHANNEL_DOWN":
                    return "OK " + tv.channelDown();
                default:
                    return "ERROR: Unknown Command";
            }
        } catch(IllegalStateException e){
            return "ERROR: " + e.getMessage();
        } catch(IllegalArgumentException e){
            return "ERROR: " + e.getMessage();
        } catch(Exception e){
            return "ERROR: Invalid command format";
        }
    }
}
