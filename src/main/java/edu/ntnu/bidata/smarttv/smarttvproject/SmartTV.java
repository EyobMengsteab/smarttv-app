package edu.ntnu.bidata.smarttv.smarttvproject;

/**
 * Represents a simple smart TV with channel and power controls.
 * <p>
 * Supports turning on/off, changing channels, and querying state.
 * Throws exceptions for invalid operations or when the TV is off.
 * </p>
 */
public class SmartTV {
    private boolean isOn = false;
    private int currentChannel = 1;
    private final int totalChannels;

    /**
     * Constructs a SmartTV with the specified number of channels.
     *
     * @param totalChannels the total number of channels; must be >= 1
     * @throws IllegalArgumentException if {@code totalChannels} is less than 1
     */
    public SmartTV(int totalChannels) {
        if (totalChannels < 1) {
            throw new IllegalArgumentException("Numbers of Channels must be >= 1");
        }
        this.totalChannels = totalChannels;
    }

    /**
     * Turns the TV on.
     */
    public void turnOn() {
        isOn = true;
    }

    /**
     * Turns the TV off.
     */
    public void turnOff() {
        isOn = false;
    }

    /**
     * Returns whether the TV is on.
     *
     * @return {@code true} if the TV is on; {@code false} otherwise
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * Returns the total number of channels.
     *
     * @return the number of channels
     * @throws IllegalStateException if the TV is off
     */
    public int getNumberOfChannels() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        return totalChannels;
    }

    /**
     * Returns the current channel.
     *
     * @return the current channel number
     * @throws IllegalStateException if the TV is off
     */
    public int getChannel() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        return currentChannel;
    }

    /**
     * Sets the TV to the specified channel.
     *
     * @param channel the channel number to set
     * @throws IllegalStateException if the TV is off
     * @throws IllegalArgumentException if the channel number is invalid
     */
    public void setChannel(int channel) {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        if (channel < 1 || channel > totalChannels)
            throw new IllegalArgumentException("Invalid channel number");
        currentChannel = channel;
    }

    /**
     * Increments the channel number by one, up to the maximum.
     *
     * @return the new channel number
     * @throws IllegalStateException if the TV is off
     */
    public int channelUp() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        if (currentChannel < totalChannels) {
            currentChannel++;
        }
        return currentChannel;
    }

    /**
     * Decrements the channel number by one, down to the minimum.
     *
     * @return the new channel number
     * @throws IllegalStateException if the TV is off
     */
    public int channelDown() {
        if (!isOn) throw new IllegalStateException("TV is OFF");
        if (currentChannel > 1) {
            currentChannel--;
        }
        return currentChannel;
    }
}
