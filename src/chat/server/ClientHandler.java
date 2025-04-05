package chat.server;

import chat.common.Message;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClientHandler> clients;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // First message from client should be the username
            this.username = in.readLine();
            broadcastSystemMessage(username + " has joined the chat.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String msg;
        try {
            while ((msg = in.readLine()) != null) {
                Message message = new Message(username, msg);
                broadcastMessage(message.format());
            }
        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}
            clients.remove(this);
            broadcastSystemMessage(username + " has left the chat.");
        }
    }

    private void broadcastMessage(String msg) {
        for (ClientHandler client : clients) {
            if (client != this) {
                client.out.println(msg);
            }
        }
    }

    private void broadcastSystemMessage(String msg) {
        for (ClientHandler client : clients) {
            if (client != this) {
                client.out.println("[System] " + msg);
            }
        }
        System.out.println("[System] " + msg);
    }
}