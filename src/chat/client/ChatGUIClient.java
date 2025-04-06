package src.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatGUIClient extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JTextField nicknameField;
    private JButton connectButton;
    private JButton disconnectButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread messageListener;
    private volatile boolean connected = false;

    public ChatGUIClient() {
        setTitle("Chat Client");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.setEnabled(false);
        inputField.addActionListener(e -> sendMessage());

        nicknameField = new JTextField();
        nicknameField.setPreferredSize(new Dimension(100, 25));
        nicknameField.setToolTipText("Nickname");

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());

        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnectFromServer());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(nicknameField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(chatScroll, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            showMessage("Enter a nickname before connecting.");
            return;
        }

        try {
            socket = new Socket("localhost", 1234);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(nickname); // Send nickname to server
            connected = true;
            inputField.setEnabled(true);
            nicknameField.setEnabled(false);
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);

            messageListener = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        chatArea.append("[" + timestamp + "] " + serverMessage + "\n");
                    }
                } catch (IOException e) {
                    if (connected) {
                        showMessage("Connection lost.");
                        disconnectFromServer();
                    }
                }
            });
            messageListener.start();

        } catch (IOException e) {
            showMessage("Unable to connect to server: " + e.getMessage());
        }
    }

    private void sendMessage() {
        if (!connected) return;

        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void disconnectFromServer() {
        if (!connected) return;

        connected = false;
        try {
            if (out != null) {
                out.println("/exit");
            }
            if (socket != null) socket.close();
            if (messageListener != null) messageListener.interrupt();
        } catch (IOException e) {
            showMessage("Error during disconnect: " + e.getMessage());
        }

        inputField.setEnabled(false);
        nicknameField.setEnabled(true);
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        showMessage("Disconnected from chat.");
    }

    private void showMessage(String message) {
        chatArea.append("[System] " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatGUIClient::new);
    }
}

