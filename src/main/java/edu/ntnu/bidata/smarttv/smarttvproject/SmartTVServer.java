package edu.ntnu.bidata.smarttv.smarttvproject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The {@code SmartTVServer} class implements a TCP server for controlling a shared {@link SmartTV} instance.
 * <p>
 * The server listens for client connections on a specified port (default is 1238, or provided as the first command-line argument),
 * processes remote control commands from each client, and sends responses back. Each client connection is handled in a separate thread.
 * The server also broadcasts state and channel changes to all connected clients.
 * </p>
 */
public class SmartTVServer {
    /**
     * The shared SmartTV instance controlled by all clients.
     */
    private static final SmartTV sharedTV = new SmartTV(5);

    /**
     * The protocol handler for processing client commands.
     */
    private static final ProtocolHandler handler = new ProtocolHandler();

    /**
     * List of output streams to all connected clients for broadcasting notifications.
     */
    private static final CopyOnWriteArrayList<PrintWriter> clients = new CopyOnWriteArrayList<>();

    /**
     * Starts the Smart TV server on the specified port (default is 1238).
     * <p>
     * Accepts incoming client connections and handles each in a separate thread.
     * </p>
     *
     * @param args Command-line arguments; the first argument specifies the server port.
     * @throws IOException if an I/O error occurs when opening the server socket.
     */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1238;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Smart TV Server running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
//        ServerSocket serverSocket = new ServerSocket(port);
//        System.out.println("Smart TV Server started on port " + port);
//
//        while (true) {
//            Socket clientSocket = serverSocket.accept();
//            new Thread(() -> handleClient(clientSocket)).start();
//        }
    }

    /**
     * Handles communication with a single client.
     * <p>
     * Reads commands from the client, processes them using the {@link ProtocolHandler},
     * and sends responses back to the client. Logs each received command and response.
     * </p>
     *
     * @param socket The client socket.
     */
    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
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
            removeClient(socket);
        }
    }

    /**
     * Checks if a command should trigger a broadcast and sends notifications to all clients.
     *
     * @param cmd      The command received from a client.
     * @param response The response sent to the client.
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
                       upper.startsWith("CHANNEL_UP")) {
              String[] parts = response.split(" ");
              if (parts.length >= 2) broadcast("NOTIFY CHANNEL " + parts[1]);
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast.
     */
    private static void broadcast(String mesage) {
        System.out.println("Broadcasting: " + mesage);
        for (PrintWriter clientOut : clients) {
            try {
                clientOut.println(mesage);
            } catch (Exception e) {
                System.out.println("Removing broken client: " + e.getMessage());
                clients.remove(clientOut);
            }
        }
    }

    /**
     * Removes a client from the list and closes its socket.
     *
     * @param socket The client socket to remove.
     */
    private static void removeClient(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {}
    }
}
