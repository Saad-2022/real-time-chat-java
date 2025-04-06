package src.chat.client;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to chat server at " + SERVER_IP + ":" + SERVER_PORT);

            // Thread to read messages from server
            Thread readThread = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = serverReader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed or error reading from server.");
                }
            });

            // Thread to send user input to server
            Thread writeThread = new Thread(() -> {
                String userMessage;
                try {
                    while ((userMessage = userInputReader.readLine()) != null) {
                        serverWriter.println(userMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message to server.");
                }
            });

            readThread.start();
            writeThread.start();

            // Wait for both threads to finish
            readThread.join();
            writeThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Unable to connect to server: " + e.getMessage());
        }
    }
}

