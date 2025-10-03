package edu.ntnu.bidata.smarttv.smarttvproject;

import java.io.*;
import java.net.*;


/**
 * The {@code SmartRemoteClient} class provides a TCP client for interacting with a {@link SmartTVServer}.
 * <p>
 * Connects to the server using a hostname and port specified as command-line arguments.
 * Allows users to enter remote control commands interactively, sends them to the server,
 * and prints the server's responses. Supports a help menu and clean disconnection.
 * </p>
 */
class SmartRemoteClient {

    /**
     * Prints available remote control commands to the console.
     */
    private static void printInstructions() {
        System.out.println("Available commands:");
        System.out.println("  HELP              - Show this help message");
        System.out.println("  TURN_ON           - Turn the TV on");
        System.out.println("  TURN_OFF          - Turn the TV off");
        System.out.println("  GET_STATE         - Get TV power state");
        System.out.println("  GET_CHANNEL       - Get current channel");
        System.out.println("  GET_CHANNELS      - Get number of channels");
        System.out.println("  SET_CHANNEL <n>   - Set channel to <n>");
        System.out.println("  CHANNEL_UP        - Go to next channel");
        System.out.println("  CHANNEL_DOWN      - Go to previous channel");
        System.out.println("  EXIT              - Disconnect from server");
        System.out.println();
    }

    /**
     * Entry point for the SmartRemoteClient application.
     * <p>
     * Connects to the Smart TV server at the given hostname and port, then enters an interactive loop
     * where the user can send commands and receive responses. Typing HELP shows available commands,
     * and EXIT disconnects from the server.
     * </p>
     *
     * @param args command-line arguments: {@code <hostname> <port>}
     * @throws IOException if an I/O error occurs when connecting to the server
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java SmartRemoteClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (
            Socket socket = new Socket(hostname, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {

            System.out.println("Connected to Smart TV Server. Enter commands (type EXIT to quit and type help to instructions):");
            String command;
            while (true) {
                System.out.print("> ");
                command = userInput.readLine();
                if (command == null || command.equalsIgnoreCase("EXIT")) break;
                if (command.equalsIgnoreCase("HELP")) {
                    printInstructions();
                    continue;
                }
                out.println(command);
                String response = in.readLine();
                System.out.println("Server: " + response);
                }
        }
    }
}
