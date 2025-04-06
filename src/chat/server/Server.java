package src.chat.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Server {
    private static final int PORT = 1234;
    private static Vector<ClientHandler> clients = new Vector<>();
    private static Set<String> nicknames = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Server running on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection: " + clientSocket);

                ClientHandler clientThread = new ClientHandler(clientSocket);
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

    public static void broadcastUserList() {
        StringBuilder sb = new StringBuilder("/users:");
        for (ClientHandler client : clients) {
            sb.append(client.getNickname()).append(",");
        }
        if (!clients.isEmpty()) {
            sb.setLength(sb.length() - 1); // remove trailing comma
        }

        String userListMessage = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(userListMessage);
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        nicknames.remove(client.getNickname());
        System.out.println("Client disconnected: " + client.getNickname());
        broadcast("[" + client.getNickname() + "] has left the chat.", null);
        broadcastUserList();
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getNickname() {
            return nickname;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Get nickname
                while (true) {
                    out.println("Enter your nickname:");
                    nickname = in.readLine();

                    if (nickname == null || nickname.trim().isEmpty()) {
                        out.println("Nickname cannot be empty.");
                    } else if (nicknames.contains(nickname)) {
                        out.println("Nickname already in use. Choose another.");
                    } else {
                        nicknames.add(nickname);
                        clients.add(this);
                        out.println("Welcome, " + nickname + "!");
                        broadcast("[" + nickname + "] has joined the chat.", this);
                        broadcastUserList();
                        break;
                    }
                }

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    if (clientMessage.startsWith("/pm ")) {
                        handlePrivateMessage(clientMessage);
                    } else {
                        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        String formattedMessage = "[" + timestamp + "] [" + nickname + "]: " + clientMessage;
                        System.out.println(formattedMessage);
                        broadcast(formattedMessage, this);
                    }
                }

            } catch (IOException e) {
                System.err.println("Connection error with client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Socket close failed: " + e.getMessage());
                }
                removeClient(this);
            }
        }

        private void handlePrivateMessage(String message) {
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                sendMessage("[System] Invalid /pm command. Use: /pm <user> <message>");
                return;
            }

            String targetUser = parts[1];
            String privateMsg = parts[2];
            ClientHandler targetClient = null;

            for (ClientHandler client : clients) {
                if (client.getNickname().equalsIgnoreCase(targetUser)) {
                    targetClient = client;
                    break;
                }
            }

            if (targetClient == null) {
                sendMessage("[System] User '" + targetUser + "' not found.");
            } else {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                targetClient.sendMessage("[" + timestamp + "] [PM from " + nickname + "]: " + privateMsg);
                sendMessage("[" + timestamp + "] [PM to " + targetUser + "]: " + privateMsg);
            }
        }
    }
}
