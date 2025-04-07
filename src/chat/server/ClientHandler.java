package chat.server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClientHandler> clients;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating streams", e);
            close();
        }
    }

    @Override
    public void run() {
        try {
            // Your message handling code goes here.
            String message;
            while (running && (message = in.readLine()) != null) {
                // Process message...
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading from client", e);
        } finally {
            close();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    // Implement the close method to properly shut down resources
    public void close() {
        running = false;
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing input stream", e);
        }
        if (out != null) {
            out.close();
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing client socket", e);
        }
        // Optionally remove this handler from the list of clients if needed
        clients.remove(this);
    }
}
