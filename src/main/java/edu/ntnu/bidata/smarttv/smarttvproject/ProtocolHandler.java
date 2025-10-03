package edu.ntnu.bidata.smarttv.smarttvproject;

/**
 * Handles remote control protocol commands for a {@link SmartTV} instance.
 * <p>
 * Supports commands for power, channel operations, and state queries.
 * Returns protocol-compliant responses or error messages.
 * </p>
 */
public class ProtocolHandler {

    /**
     * Processes a command string and applies it to the given {@link SmartTV}.
     * <p>
     * Recognized commands include TURN_ON, TURN_OFF, GET_STATE, GET_CHANNEL, GET_CHANNELS,
     * SET_CHANNEL, CHANNEL_UP, and CHANNEL_DOWN. Returns an OK or ERROR response.
     * </p>
     *
     * @param input the command string from the client
     * @param tv the {@link SmartTV} to control
     * @return the response string for the command
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
                    return "OK " + tv.getNumberOfChannels();
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
