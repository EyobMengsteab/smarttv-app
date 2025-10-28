package edu.ntnu.bidata.smarttv.smarttvproject;

/**
 * Represents a Smart TV device with comprehensive channel and power management capabilities.
 * <p>
 * The {@code SmartTV} class models a television device that supports power control,
 * channel navigation, and state management. It maintains an internal list of named
 * channels (e.g., "BBC", "NRK") and enforces proper operational constraints
 * such as preventing channel operations when the TV is powered off.
 * </p>
 *
 * <p>
 * Key features include:
 * <ul>
 *   <li>Power management (ON/OFF states)</li>
 *   <li>Named channel support with predefined channel names</li>
 *   <li>Channel navigation (up/down/direct selection)</li>
 *   <li>State validation with appropriate exception handling</li>
 *   <li>Boundary checking for channel operations</li>
 * </ul>
 * </p>
 *
 * <p>
 * The TV enforces operational rules where channel-related operations are only
 * permitted when the device is powered on. Attempting to access channels while
 * the TV is off will result in an {@link IllegalStateException}.
 * </p>
 *
 * @author EyobMengsteab
 */
public class SmartTV {
    private boolean isOn = false;
    private int currentChannel = 1;
    private final String[] channelNames;
    private final int totalChannels;

    /**
     * Constructs a new SmartTV with the specified channel names.
     * <p>
     * Initializes the TV in the OFF state with the current channel set to 1.
     * The provided channel names define the available channels for this TV instance.
     * Channel names are copied defensively to prevent external modification.
     * </p>
     *
     * <p>
     * <strong>Validation:</strong> The channel names array must not be null
     * and must contain at least one channel name to ensure basic TV functionality.
     * </p>
     *
     * @param channelNames array of channel names to be available on this TV.
     *                     Must not be {@code null} and must contain at least one element.
     *                     Channel names will be copied defensively
     * @throws IllegalArgumentException if {@code channelNames} is {@code null}
     *                                  or contains fewer than 1 element
     */
    public SmartTV(String[] channelNames) {
        if (channelNames == null || channelNames.length < 1) {
            throw new IllegalArgumentException("Must have at least 1 channel");
        }
        this.channelNames = channelNames.clone();
        this.totalChannels = channelNames.length;
    }

    /**
     * Powers on the TV device.
     * <p>
     * Transitions the TV from OFF to ON state, enabling all channel operations.
     * If the TV is already on, this method has no effect. After turning on,
     * the current channel remains at its previous value or defaults to channel 1
     * for a new TV instance.
     * </p>
     */
    public void turnOn() {
        isOn = true;
    }

    /**
     * Powers off the TV device.
     * <p>
     * Transitions the TV from ON to OFF state, disabling all channel operations.
     * The current channel number is preserved and will be restored when the TV
     * is turned back on. If the TV is already off, this method has no effect.
     * </p>
     */
    public void turnOff() {
        isOn = false;
    }

    /**
     * Returns the current power state of the TV.
     * <p>
     * This method can be called regardless of the TV's power state and provides
     * a way to check whether channel operations are permitted.
     * </p>
     *
     * @return {@code true} if the TV is powered on and operational,
     *         {@code false} if the TV is powered off
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * Returns a copy of all available channel names.
     * <p>
     * Provides access to the complete list of channel names that were configured
     * during TV construction. The returned array is a defensive copy to prevent
     * external modification of the internal channel list.
     * </p>
     * <p>
     * <strong>Array Structure:</strong> Channel names are returned in their original
     * order with array indices corresponding to channel numbers minus 1 (e.g.,
     * array index 0 represents channel 1).
     * </p>
     *
     * @return a copy of the channel names array. Never {@code null}
     * @throws IllegalStateException if the TV is powered off
     */
    public String[] getChannelNames() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        return channelNames.clone();
    }

    /**
     * Returns the total number of channels available on this TV.
     * <p>
     * This value corresponds to the length of the channel names array provided
     * during construction and represents the valid range for channel operations
     * (1 to this number inclusive).
     * </p>
     *
     * @return the total number of channels, always greater than 0
     * @throws IllegalStateException if the TV is powered off
     */
    public int getNumberOfChannels() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        return totalChannels;
    }

    /**
     * Returns the currently selected channel number.
     * <p>
     * Channel numbers are 1-based and correspond to positions in the channel
     * names array. For example, channel 1 refers to the first channel name,
     * channel 2 to the second, and so on.
     * </p>
     *
     * @return the current channel number, between 1 and {@link #getNumberOfChannels()} inclusive
     * @throws IllegalStateException if the TV is powered off
     */
    public int getChannel() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        return currentChannel;
    }

    /**
     * Sets the TV to the specified channel number.
     * <p>
     * Changes the current channel to the specified number. Channel numbers are
     * 1-based and must fall within the valid range of available channels.
     * The change takes effect immediately.
     * </p>
     *
     * <p>
     * <strong>Validation:</strong> The channel number must be within the valid
     * range (1 to total number of channels). Invalid channel numbers will
     * result in an exception and the current channel will remain unchanged.
     * </p>
     *
     * @param channel the channel number to switch to. Must be between 1 and
     *               {@link #getNumberOfChannels()} inclusive
     * @throws IllegalStateException    if the TV is powered off
     * @throws IllegalArgumentException if the channel number is outside the valid range
     */
    public void setChannel(int channel) {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        if (channel < 1 || channel > totalChannels)
            throw new IllegalArgumentException("Invalid channel number");
        currentChannel = channel;
    }

    /**
     * Increments the current channel by one.
     * <p>
     * Moves to the next higher channel number if available. If the TV is already
     * on the highest channel, this method has no effect and returns the current
     * channel number. This provides a "wrap-around" prevention behavior.
     * </p>
     *
     * <p>
     * <strong>Boundary Behavior:</strong> When called on the maximum channel,
     * the channel number remains unchanged rather than wrapping to channel 1.
     * </p>
     *
     * @return the new current channel number after the operation
     * @throws IllegalStateException if the TV is powered off
     */
    public int channelUp() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        if (currentChannel < totalChannels) {
            currentChannel++;
        }
        return currentChannel;
    }

    /**
     * Decrements the current channel by one.
     * <p>
     * Moves to the next lower channel number if available. If the TV is already
     * on the lowest channel (channel 1), this method has no effect and returns
     * the current channel number. This provides a "wrap-around" prevention behavior.
     * </p>
     *
     * <p>
     * <strong>Boundary Behavior:</strong> When called on channel 1, the channel
     * number remains unchanged rather than wrapping to the maximum channel.
     * </p>
     *
     * @return the new current channel number after the operation
     * @throws IllegalStateException if the TV is powered off
     */
    public int channelDown() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        if (currentChannel > 1) {
            currentChannel--;
        }
        return currentChannel;
    }
}
