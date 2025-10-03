package edu.ntnu.bidata.smarttv.smarttvproject;

import java.io.*;
import java.net.*;

/**
 * The {@code SmartTVServer} class implements a TCP server for controlling a shared {@link SmartTV} instance.
 * <p>
 * The server listens for client connections on a specified port (default is 1238, or provided as the first command-line argument),
 * processes remote control commands from each client, and sends responses back. Each client connection is handled in a separate thread.
 * </p>
 */
public class SmartTVServer {
    private static final SmartTV sharedTV = new SmartTV(5);
    private static final ProtocolHandler handler = new ProtocolHandler();

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
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Smart TV Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
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
            String input;
            while ((input = in.readLine()) != null) {
                String response = handler.handleCommand(input, sharedTV);
                System.out.println("Received: " + input + " | Responded: " + response);
                out.println(response);
            }
            // Inside handleClient, after reading and handling a command:
            while ((input = in.readLine()) != null) {
                String response = handler.handleCommand(input, sharedTV);
                System.out.println("Received: " + input + " | Responded: " + response);
                out.println(response);
            }
        } // Replace the catch block in handleClient:
        catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }
}
