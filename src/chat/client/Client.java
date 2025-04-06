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
            System.out.print("Enter your nickname: ");
            String nickname = userInputReader.readLine();
            serverWriter.println(nickname);  // Send nickname to server

            System.out.println("Connected to chat server as " + nickname);
            System.out.println("Type '/exit' to leave the chat.");

            Thread readThread = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = serverReader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Disconnected from server.");
                }
            });

            Thread writeThread = new Thread(() -> {
                String userMessage;
                try {
                    while ((userMessage = userInputReader.readLine()) != null) {
                        if (userMessage.equalsIgnoreCase("/exit")) {
                            System.out.println("You have left the chat.");
                            socket.close();
                            break;
                        }
                        serverWriter.println(userMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message to server.");
                }
            });

            readThread.start();
            writeThread.start();

            readThread.join();
            writeThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}

