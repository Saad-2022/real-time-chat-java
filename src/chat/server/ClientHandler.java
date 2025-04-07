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

            sendUserList(); // send existing users to the new client
            broadcast("JOIN " + username); // send JOIN signal to all clients (for avatar)
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
                Message message = new Message(username, msg);
                broadcast(message.format());
            }
        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {}

            clients.remove(this);
            broadcast("LEAVE " + username);
            broadcastSystemMessage("[System] " + username + " has left the chat.");
        }
    }

    private void sendUserList() {
        for (ClientHandler client : clients) {
            if (!client.username.equals(this.username)) {
                out.println("JOIN " + client.username); // send other users to the new client
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