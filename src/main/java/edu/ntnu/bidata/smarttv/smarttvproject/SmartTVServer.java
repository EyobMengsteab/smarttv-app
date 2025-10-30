package edu.ntnu.bidata.smarttv.smarttvproject;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * A multi-threaded TCP server for managing remote control operations on a shared Smart TV.
 * <p>
 * The {@code SmartTVServer} class implements a concurrent server that allows multiple clients
 * to simultaneously control a single {@link SmartTV} instance over network connections. The
 * server processes protocol commands from clients, manages TV state, and broadcasts state
 * changes to all connected clients for real-time synchronization.
 * </p>
 *
 * <p>
 * Key features include:
 * <ul>
 *   <li>Multi-client support with thread-safe operations</li>
 *   <li>Real-time broadcasting of TV state changes</li>
 *   <li>Automatic client connection management</li>
 *   <li>Protocol-based command processing via {@link ProtocolHandler}</li>
 *   <li>Graceful handling of client disconnections</li>
 *   <li>Configurable server port via command-line arguments</li>
 * </ul>
 * </p>
 *
 * <p>
 * The server maintains a shared {@link SmartTV} instance with predefined channels
 * (BBC, National Geographic, NRK, Discovery Science, History) that all clients
 * can control. When one client changes the TV state or channel, all other connected
 * clients receive notification messages to keep their interfaces synchronized.
 * </p>
 *
 * <p>
 * <strong>Protocol Support:</strong> The server processes standard Smart TV protocol
 * commands including power control (TURN_ON/TURN_OFF), channel operations
 * (CHANNEL_UP/CHANNEL_DOWN/SET_CHANNEL), and state queries (GET_STATE/GET_CHANNEL).
 * </p>
 *
 * <p>
 * <strong>Thread Safety:</strong> This server uses {@link CopyOnWriteArrayList} for
 * client management and separate threads for each client connection to ensure
 * thread-safe operations without blocking.
 * </p>
 *
 * @author EyobMengsteab
 * @version 2025-10-30
 */
public class SmartTVServer {

    /**
     * Predefined channel names available on the shared Smart TV.
     * <p>
     * This array defines the channels available to all clients connecting to the server.
     * The channels are: BBC, National Geographic, NRK, Discovery Science, and History.
     * These names are used to initialize the shared {@link SmartTV} instance.
     * </p>
     */
    private static final String[] channels = {"BBC", "NATIONAL GEOGRAPHIC", "NRK", "Discovery Science", "History"};

    /**
     * The shared Smart TV instance controlled by all connected clients.
     * <p>
     * This single {@link SmartTV} instance is shared among all client connections,
     * ensuring that state changes made by one client are immediately visible to
     * all other clients. The TV is initialized with the predefined channel names
     * and starts in the OFF state.
     * </p>
     */
    private static final SmartTV sharedTV = new SmartTV(channels);

    /**
     * Protocol handler for processing client commands.
     * <p>
     * This {@link ProtocolHandler} instance is responsible for parsing client
     * commands and executing the appropriate operations on the shared TV instance.
     * It provides standardized response formatting and error handling.
     * </p>
     */
    private static final ProtocolHandler handler = new ProtocolHandler();

    /**
     * Thread-safe list of output streams for all connected clients.
     * <p>
     * This list maintains {@link PrintWriter} instances for each connected client
     * to enable broadcasting of notification messages. Uses {@link CopyOnWriteArrayList}
     * to ensure thread-safe concurrent access during client addition, removal, and
     * message broadcasting operations.
     * </p>
     */
    private static final CopyOnWriteArrayList<PrintWriter> clients = new CopyOnWriteArrayList<>();

    /**
     * Default server port number.
     * <p>
     * The server will listen on this port if no port number is provided
     * via command-line arguments.
     * </p>
     */
    private static final int DEFAULT_PORT = 1238;

    /**
     * Starts the Smart TV server and begins accepting client connections.
     * <p>
     * This method initializes a {@link ServerSocket} on the specified port and
     * enters an infinite loop to accept incoming client connections. Each client
     * connection is handled in a separate daemon thread to allow concurrent
     * access to the shared TV instance.
     * </p>
     * <p>
     * <strong>Port Configuration:</strong> The server port can be specified as the
     * first command-line argument. If no argument is provided, the default port
     * {@value #DEFAULT_PORT} is used.
     * </p>
     * <p>
     * <strong>Client Handling:</strong> Each accepted client connection spawns a
     * new thread that handles all communication with that specific client until
     * the client disconnects or an error occurs.
     * </p>
     *
     * @param args command-line arguments where the first argument optionally
     *             specifies the server port number. If not provided, uses
     *             {@value #DEFAULT_PORT}
     * @throws IOException              if an I/O error occurs when opening the server socket
     * @throws NumberFormatException    if the provided port argument is not a valid integer
     * @throws IllegalArgumentException if the port number is outside the valid range (1-65535)
     */
    public static void main(String[] args) throws IOException {

        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Smart TV Server running on port " + port);
            System.out.println("Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    /**
     * Handles all communication with a single connected client.
     * <p>
     * This method runs in a separate thread for each client connection and manages
     * the complete lifecycle of client communication. It reads commands from the
     * client's input stream, processes them through the {@link ProtocolHandler},
     * sends responses back to the client, and handles broadcasting of state changes
     * to other connected clients.
     * </p>
     * <p>
     * <strong>Communication Flow:</strong>
     * <ol>
     *   <li>Register the client's output stream for broadcasting</li>
     *   <li>Read commands from the client in a loop</li>
     *   <li>Process each command using the protocol handler</li>
     *   <li>Send the response back to the client</li>
     *   <li>Check if the command requires broadcasting to other clients</li>
     *   <li>Continue until the client disconnects</li>
     * </ol>
     * </p>
     *
     * <p>
     * <strong>Error Handling:</strong> The method gracefully handles client
     * disconnections and I/O errors, ensuring proper cleanup of resources
     * and removal of the client from the broadcast list.
     * </p>
     *
     * @param socket the client socket connection to handle. Must not be {@code null}
     * @throws IllegalArgumentException if the socket is {@code null}
     */
    private static void handleClient(Socket socket) {
        PrintWriter out = null;
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            out = new PrintWriter(socket.getOutputStream(), true);
            clients.add(out); // register this client

            String input;
            while ((input = in.readLine()) != null) {
                String response = handler.handleCommand(input, sharedTV);
                out.println(response);
                System.out.println("Received: " + input + " | Responded: " + response);
                checkAndBroadcast(input.trim(), response);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            if (out != null) {
                clients.remove(out); // Properly remove the PrintWriter
            }
            removeClient(socket);
        }
    }

    /**
     * Determines if a command requires broadcasting and sends notifications to all clients.
     * <p>
     * This method analyzes successful command responses to identify state-changing
     * operations that should be broadcast to all connected clients. This ensures
     * that all client interfaces remain synchronized with the actual TV state.
     * </p>
     * <p>
     * <strong>Broadcast Triggers:</strong>
     * <ul>
     *   <li><strong>Power Changes:</strong> TURN_ON and TURN_OFF commands trigger
     *       "NOTIFY STATE [ON|OFF]" broadcasts</li>
     *   <li><strong>Channel Changes:</strong> SET_CHANNEL, CHANNEL_UP, and CHANNEL_DOWN
     *       commands trigger "NOTIFY CHANNEL [number]" broadcasts</li>
     * </ul>
     * </p>
     * <p>
     * Only successful operations (responses starting with "OK") trigger broadcasts
     * to prevent propagating error states to other clients.
     * </p>
     *
     * @param cmd      the original command string received from the client.
     *                 Used to determine the command type for broadcast logic
     * @param response the response string that was sent back to the client.
     *                 Must start with "OK" for broadcasts to be triggered
     */
    private static void checkAndBroadcast(String cmd, String response) {
        String upper = cmd.toUpperCase();
        if ( response.startsWith("OK")) {
            if (upper.startsWith("TURN_ON")) {
                broadcast("NOTIFY STATE ON");
            } else if (upper.startsWith("TURN_OFF")) {
                broadcast("NOTIFY STATE OFF");
            } else if (upper.startsWith("SET_CHANNEL") ||
                       upper.startsWith("CHANNEL_UP") ||
                       upper.startsWith("CHANNEL_DOWN")) {
              String[] parts = response.split(" ");
              if (parts.length >= 2) broadcast("NOTIFY CHANNEL " + parts[1]);
            }
        }
    }

    /**
     * Broadcasts a notification message to all connected clients.
     * <p>
     * This method sends the specified message to every client currently registered
     * in the {@link #clients} list. It handles potential communication errors
     * gracefully by removing clients that can no longer receive messages (e.g.,
     * due to network issues or unexpected disconnections).
     * </p>
     * <p>
     * <strong>Error Handling:</strong> If sending a message to any client fails,
     * that client's {@link PrintWriter} is automatically removed from the client
     * list to prevent future broadcast attempts to the failed connection.
     * </p>
     * <p>
     * <strong>Thread Safety:</strong> This method is thread-safe due to the use
     * of {@link CopyOnWriteArrayList} for the client list, allowing concurrent
     * broadcasting while other threads modify the client list.
     * </p>
     *
     * @param message the notification message to send to all clients.
     *                Typically follows the format "NOTIFY [TYPE] [VALUE]"
     */
    private static void broadcast(String message) {
        System.out.println("Broadcasting: " + message);
        for (PrintWriter clientOut : clients) {
            try {
                clientOut.println(message);
            } catch (Exception e) {
                System.out.println("Removing broken client: " + e.getMessage());
                clients.remove(clientOut);
            }
        }
    }

    /**
     * Properly closes a client socket connection and performs cleanup.
     * <p>
     * This method ensures that client socket resources are properly released
     * when a client disconnects or encounters an error. It checks if the socket
     * is already closed before attempting to close it, preventing unnecessary
     * exceptions.
     * </p>
     *
     * <p>
     * <strong>Resource Management:</strong> This method should be called in the
     * finally block of client handling code to guarantee proper resource cleanup
     * regardless of how the client session ends (normal disconnection, error, etc.).
     * </p>
     *
     * @param socket the client socket to close and clean up.
     *               If {@code null} or already closed, no action is taken
     */
    private static void removeClient(Socket socket) {
        if (socket == null) {
            return;
        }

        try {
            if (!socket.isClosed()) {
                socket.close();
                System.out.println("Closed connection to client: " + socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}
