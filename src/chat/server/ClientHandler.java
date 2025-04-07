package chat.server;

import chat.common.Message;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClientHandler> clients;
    public String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // First message from client should be username
            this.username = in.readLine();

            sendUserList();
            broadcast("JOIN " + username);
            broadcastSystemMessage("[System] " + username + " has joined the chat.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String msg;
        try {
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("/pm ")) {
                    handlePrivateMessage(msg);
                } else {
                    Message message = new Message(username, msg);
                    broadcast(message.format());
                }
            }
        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }

            clients.remove(this);
            broadcast("LEAVE " + username);
            broadcastSystemMessage("[System] " + username + " has left the chat.");
        }
    }

    private void handlePrivateMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            out.println("[System] Usage: /pm <user> <message>");
            return;
        }
        String targetUsername = parts[1];
        String privateMsg = parts[2];

        for (ClientHandler client : clients) {
            if (client.username.equalsIgnoreCase(targetUsername)) {
                client.out.println("[PM from " + username + "] " + privateMsg);
                this.out.println("[PM to " + targetUsername + "] " + privateMsg);
                return;
            }
        }
        out.println("[System] User not found: " + targetUsername);
    }

    private void sendUserList() {
        for (ClientHandler client : clients) {
            if (!client.username.equals(this.username)) {
                out.println("JOIN " + client.username);
            }
        }
    }

    private void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.out.println(msg);
            }
        }
    }

    private void broadcastSystemMessage(String msg) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.out.println(msg);
            }
        }
    }
}
