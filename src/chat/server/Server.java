package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final int PORT = 12345;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final ExecutorService clientPool = Executors.newCachedThreadPool();
    private static volatile boolean running = true;

    public static void main(String[] args) {
        logger.info("Server starting on port " + PORT);

        // Adding shutdown hook to handle graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(Server::shutdown));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    ClientHandler handler = new ClientHandler(clientSocket, clients);
                    clients.add(handler);
                    clientPool.execute(handler);
                } catch (IOException e) {
                    if (running) {
                        logger.log(Level.SEVERE, "Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server socket error", e);
        } finally {
            shutdown();
        }
    }

    private static void shutdown() {
        if (!running) {
            return;
        }
        running = false;
        logger.info("Server shutting down...");
        // Close all client connections
        for (ClientHandler client : clients) {
            client.close();
        }
        // Shutdown the thread pool gracefully
        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
        }
        logger.info("Server shutdown complete.");
    }
}
