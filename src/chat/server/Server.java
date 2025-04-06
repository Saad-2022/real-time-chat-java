package src.chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    private static Vector<ClientHandler> clients = new Vector<>();

    public static void main(String[] args) {
        System.out.println("Server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientThread = new ClientHandler(clientSocket);
                clients.add(clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected.");
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Error setting up I/O for client: " + e.getMessage());
            }
        }

        public void run() {
            String message;
            try {
                out.println("Connected to chat server.");
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    Server.broadcast(message, this);
                }
            } catch (IOException e) {
                System.err.println("Client disconnected unexpectedly: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close socket: " + e.getMessage());
                }
                Server.removeClient(this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
