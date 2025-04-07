package chat.client;

import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader userInput;

    public ChatClient(String host, int port) {
        try {
            // Connect to the chat server
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connected to the chat server at " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        // Thread to continuously read messages from the server
        Thread readerThread = new Thread(() -> {
            try {
                String serverMsg;
                while ((serverMsg = in.readLine()) != null) {
                    System.out.println(serverMsg);
                }
            } catch (IOException e) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        });
        readerThread.start();

        // Read messages from the console and send them to the server
        try {
            String userMsg;
            while ((userMsg = userInput.readLine()) != null) {
                out.println(userMsg);
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (userInput != null) userInput.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Default host and port; these can be overridden via command-line arguments
        String host = "localhost";
        int port = 12345;
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 12345.");
            }
        }
        ChatClient client = new ChatClient(host, port);
        client.start();
    }
}
