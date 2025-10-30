package edu.ntnu.bidata.smarttv.smarttvproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

/**
 * A JavaFX-based graphical user interface for controlling a Smart TV remotely.
 * <p>
 * This class provides a comprehensive remote control interface that connects to a
 * Smart TV server over TCP/IP. It offers real-time communication with the server,
 * allowing users to control TV power, change channels, and monitor TV state through
 * an intuitive graphical interface.
 * </p>
 *
 * <p>
 * The GUI includes:
 * <ul>
 *   <li>Power control buttons (ON/OFF)</li>
 *   <li>Channel navigation (UP/DOWN/SET)</li>
 *   <li>State and channel information display</li>
 *   <li>Channel list view with click-to-select functionality</li>
 *   <li>Real-time logging of commands and server responses</li>
 *   <li>Connection status indicator</li>
 * </ul>
 * </p>
 *
 <p>
 * The application automatically establishes a connection to the Smart TV server
 * on startup and maintains bidirectional communication through separate threads
 * for sending commands and receiving server responses/notifications.
 * </p>
 *
 * @author EyobMengsteab
 */
public class SmartRemoteGUI extends Application {

    /**
     * Output stream for sending commands to the Smart TV server.
     * <p>
     * This writer is used to send protocol commands such as TURN_ON, CHANNEL_UP, etc.
     * to the connected Smart TV server.
     * </p>
     */
    private PrintWriter out;

    /**
     * Input stream for receiving responses and notifications from the Smart TV server.
     * <p>
     * This reader processes server responses and real-time notifications in a
     * separate background thread to maintain UI responsiveness.
     * </p>
     */
    private BufferedReader in;

    /**
     * Text area component for displaying command logs and server responses.
     * <p>
     * Provides a scrollable view of all communication between the client and server,
     * including sent commands, received responses, and system messages.
     * </p>
     */
    private TextArea logArea;

    /**
     * Label displaying the current power state of the TV (ON/OFF).
     * <p>
     * Updated in real-time based on server responses and notifications.
     * </p>
     */
    private Label stateLabel;

    /**
     * Label displaying the current channel number of the TV.
     * <p>
     * Reflects the active channel and is updated when channel changes occur
     * either through user commands or server notifications.
     * </p>
     */
    private Label channelLabel;

    /**
     * Label indicating the connection status to the Smart TV server.
     * <p>
     * Shows a visual indicator (colored dot and text) representing whether
     * the client is successfully connected to the server.
     * </p>
     */
    private Label connectionStatus;

    /**
     * TCP socket connection to the Smart TV server.
     * <p>
     * Maintains the network connection for bidirectional communication
     * with the Smart TV server application.
     * </p>
     */
    private Socket socket;

    /**
     * The hostname or IP address of the Smart TV server.
     * <p>
     * Default value is "localhost" for local testing. Change this value
     * if connecting to a remote server.
     * </p>
     */
    private static final String HOST = "localhost";

    /**
     * The port number on which the Smart TV server is listening.
     * <p>
     * Must match the port configured on the Smart TV server application.
     * </p>
     */
    private static final int PORT = 1238;

    /**
     * ListView component displaying available TV channels.
     * <p>
     * Shows either channel names (BBC, NRK, CN, etc.) or numbered channels (1, 2, 3, etc.)
     * depending on server configuration. Supports double-click selection to change channels.
     * </p>
     */
    private ListView<String> channelList;

    /**
     * Initializes and displays the Smart TV remote control GUI.
     * <p>
     * This method sets up the complete user interface including all controls,
     * establishes connection to the Smart TV server, and configures event handlers
     * for user interactions. The layout includes power controls, channel navigation,
     * status displays, and logging facilities.
     * </p>
     * <p>
     * The method also starts a background thread for listening to server messages
     * to ensure the UI remains responsive during network communication.
     * </p>
     *
     * @param stage the primary stage for this application, onto which
     *              the application scene can be set
     * @throws RuntimeException if unable to connect to the Smart TV server
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Smart TV Remote");

        // ===== Layout components =====
        stateLabel = new Label("State: OFF");
        channelLabel = new Label("Channel: -");

        Button powerOnBtn = new Button("Turn ON");
        Button powerOffBtn = new Button("Turn OFF");
        Button chUpBtn = new Button("Channel ▲");
        Button chDownBtn = new Button("Channel ▼");
        Button getStateBtn = new Button("Get State");
        Button getChannelBtn = new Button("Get Channel");
        Button getChannelsBtn = new Button("Get Channels");
        Button exitBtn = new Button("Exit");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        connectionStatus = new Label("●");
        connectionStatus.setText("● Connected");
        connectionStatus.setStyle("-fx-text-fill: green;");

        TextField channelInput = new TextField();
        channelInput.setPromptText("Enter channel...");
        Button setChannelBtn = new Button("Set Channel");

        setChannelBtn.setOnAction(e -> {
            String ch = channelInput.getText().trim();
            if (!ch.isEmpty()) {
                sendCommand("SET_CHANNEL " + ch);
                channelInput.clear();
            }
        });

        Tooltip toast = new Tooltip("Command sent!");
        toast.setAutoHide(true);
        toast.show(stage);

        GridPane remoteLayout = new GridPane();
        remoteLayout.setHgap(10);
        remoteLayout.setVgap(10);
        remoteLayout.setAlignment(Pos.CENTER);

        remoteLayout.add(powerOnBtn, 0, 0);
        remoteLayout.add(powerOffBtn, 1, 0);
        remoteLayout.add(chUpBtn, 0, 1);
        remoteLayout.add(chDownBtn, 1, 1);
        remoteLayout.add(getStateBtn, 0, 2);
        remoteLayout.add(getChannelBtn, 1, 2);
        remoteLayout.add(getChannelsBtn, 0, 3, 2, 1);
        remoteLayout.add(exitBtn, 0, 4, 2, 1);
        // Add to your GridPane
        remoteLayout.add(channelInput, 0, 5);
        remoteLayout.add(setChannelBtn, 1, 5);

        VBox root = new VBox(10,
                new HBox(10, new Label("Smart TV Remote Control"), connectionStatus),
                stateLabel,
                channelLabel,
                remoteLayout,
                new Label("Notifications / Logs:"),
                logArea
        );

        root.setStyle("-fx-padding: 15; -fx-font-size: 14px;");

        channelList = new ListView<>();
        channelList.setPrefHeight(200);
        root.getChildren().add(channelList);

        // Add this after creating channelList (around line 88):
        channelList.setOnMouseClicked(event -> {
            String selectedChannel = channelList.getSelectionModel().getSelectedItem();
            if (selectedChannel != null && event.getClickCount() == 2) {
                sendCommand("SET_CHANNEL " + selectedChannel);
                log("Selected channel: " + selectedChannel);
            }
        });


        // ===== Event Handlers =====
        powerOnBtn.setOnAction(e -> sendCommand("TURN_ON"));
        powerOffBtn.setOnAction(e -> sendCommand("TURN_OFF"));
        chUpBtn.setOnAction(e -> sendCommand("CHANNEL_UP"));
        chDownBtn.setOnAction(e -> sendCommand("CHANNEL_DOWN"));
        getStateBtn.setOnAction(e -> sendCommand("GET_STATE"));
        getChannelBtn.setOnAction(e -> sendCommand("GET_CHANNEL"));
        getChannelsBtn.setOnAction(e -> sendCommand("GET_CHANNELS"));
        exitBtn.setOnAction(e -> closeApp());

        // ===== Connect to Server =====
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log("Connected to Smart TV Server on " + HOST + ":" + PORT);
            startListenerThread();
        } catch (IOException e) {
            showError("Cannot connect to server: " + e.getMessage());
        }

        // ===== Setup Stage =====
        Scene scene = new Scene(root, 450, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sends a protocol command to the Smart TV server.
     * <p>
     * Transmits the specified command string to the connected server and logs
     * the command to the user interface. Commands follow the Smart TV protocol
     * format (e.g., "TURN_ON", "CHANNEL_UP", "SET_CHANNEL 5").
     * </p>
     * <p>
     * If no server connection exists, an error message is displayed instead
     * of attempting to send the command.
     * </p>
     *
     * @param cmd the protocol command string to send to the server.
     *            Must not be null or empty
     */
    private void sendCommand(String cmd) {
        if (out == null) {
            showError("Not connected to server");
            return;
        }
        out.println(cmd);
        log("> " + cmd);
    }


    /**
     * Starts a background daemon thread to listen for server messages.
     * <p>
     * This thread continuously reads from the server input stream and processes
     * incoming messages through {@link #handleServerMessage(String)}. The thread
     * runs as a daemon to ensure it doesn't prevent application shutdown.
     * </p>
     * <p>
     * All UI updates from server messages are dispatched to the JavaFX Application
     * Thread using {@link Platform#runLater(Runnable)} to maintain thread safety.
     * </p>
     */
    private void startListenerThread() {
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String msg = line.trim();
                    Platform.runLater(() -> handleServerMessage(msg));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showError("Connection closed: " + e.getMessage()));
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    /**
     * Processes incoming messages from the Smart TV server.
     * <p>
     * This method parses server responses and updates the GUI accordingly. It handles
     * different types of server messages including:
     * <ul>
     *   <li>Channel list updates (comma-separated channel names)</li>
     *   <li>Numeric responses for current channel</li>
     *   <li>State changes (ON/OFF responses)</li>
     *   <li>Server notifications for real-time updates</li>
     * </ul>
     * </p>
     * <p>
     * The method intelligently distinguishes between channel name lists and numeric
     * responses to prevent overwriting actual channel names with generated numbers.
     * </p>
     *
     * @param msg the message received from the Smart TV server.
     *            Expected to follow the protocol format (e.g., "OK BBC,NRK")
     */
    private void handleServerMessage(String msg) {
        log("Server: " + msg);

        // Handle GET_CHANNELS response - check for comma-separated values (actual channel names)
        if (msg.startsWith("OK ") && msg.contains(",")) {
            String channelData = msg.substring(3); // Remove "OK "
            String[] channels = channelData.split(",");

            // Clean up each channel (trim whitespace)
            for (int i = 0; i < channels.length; i++) {
                channels[i] = channels[i].trim();
            }

            channelList.getItems().clear();
            channelList.getItems().addAll(channels);

        } else if (msg.startsWith("OK ") && msg.matches("OK \\d+")) {
            // Handle numeric responses
            String[] parts = msg.split(" ");
            if (parts.length == 2) {
                String numberStr = parts[1];

                // Only update channel list if it's currently empty or has numbered channels
                // This prevents overwriting actual channel names with numbers
                boolean hasNamedChannels = !channelList.getItems().isEmpty() &&
                        !channelList.getItems().get(0).matches("\\d+");

                if (!hasNamedChannels) {
                    try {
                        int channelCount = Integer.parseInt(numberStr);
                        // Generate numbered channels only if we don't have named channels
                        String[] channels = new String[channelCount];
                        for (int i = 0; i < channelCount; i++) {
                            channels[i] = String.valueOf(i + 1);
                        }
                        channelList.getItems().clear();
                        channelList.getItems().addAll(channels);
                    } catch (NumberFormatException e) {
                        // Not a valid number, treat as current channel
                        channelLabel.setText("Channel: " + numberStr);
                    }
                } else {
                    // We have named channels, so this number is just the current channel
                    channelLabel.setText("Channel: " + numberStr);
                }
            }

        } else if (msg.startsWith("OK")) {
            // Handle other OK responses (ON/OFF states)
            if (msg.contains("ON")) {
                stateLabel.setText("State: ON");
            } else if (msg.contains("OFF")) {
                stateLabel.setText("State: OFF");
            }

        } else if (msg.startsWith("NOTIFY")) {
            // Handle notifications
            String[] parts = msg.split(" ");
            if (parts.length >= 3 && parts[1].equals("CHANNEL")) {
                channelLabel.setText("Channel: " + parts[2]);
            } else if (parts.length >= 3 && parts[1].equals("STATE")) {
                stateLabel.setText("State: " + parts[2]);
            }
        }
    }

    /**
     * Appends a log message to the log display area.
     * <p>
     * Provides a centralized logging mechanism for displaying command history,
     * server responses, and system messages in the GUI log area.
     * </p>
     *
     * @param text the message to log. A newline character is automatically appended
     */
    private void log(String text) {
        logArea.appendText(text + "\n");
    }

    /**
     * Displays an error message in the log area with error formatting.
     * <p>
     * Convenience method for logging error messages with consistent formatting.
     * Error messages are prefixed with "[ERROR]" for easy identification.
     * </p>
     *
     * @param msg the error message to display
     */
    private void showError(String msg) {
        log("[ERROR] " + msg);
    }

    /**
     * Cleanly closes the server connection and exits the application.
     * <p>
     * This method ensures proper cleanup by closing the TCP socket connection
     * before terminating the JavaFX application. Socket closure errors are
     * ignored to prevent blocking during shutdown.
     * </p>
     */
    private void closeApp() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        Platform.exit();
    }

    /**
     * Application entry point.
     * <p>
     * Launches the JavaFX application and displays the Smart TV remote control GUI.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}