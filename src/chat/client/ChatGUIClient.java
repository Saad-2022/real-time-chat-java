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

    private DefaultListModel<String> userListModel;
    private JList<String> userList;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread messageListener;
    private volatile boolean connected = false;

    public ChatGUIClient() {
        setTitle("Java Swing Chat Client");
        setSize(600, 450);
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

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBorder(BorderFactory.createTitledBorder("Active Users"));
        userList.setPreferredSize(new Dimension(120, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(chatScroll, BorderLayout.CENTER);
        centerPanel.add(userList, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
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

            out.println(nickname);
            connected = true;
            inputField.setEnabled(true);
            nicknameField.setEnabled(false);
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);

            messageListener = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("/users:")) {
                            updateUserList(message.substring(7));
                        } else if (message.contains("[PM from") || message.contains("[PM to")) {
                            appendStyled(message, Color.MAGENTA);
                        } else {
                            appendStyled(message, Color.BLACK);
                        }
                    }
                } catch (IOException e) {
                    if (connected) {
                        showMessage("Disconnected from server.");
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
        userListModel.clear();
        showMessage("Disconnected from chat.");
    }

    private void updateUserList(String rawUserList) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            String[] users = rawUserList.split(",");
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    private void appendStyled(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            chatArea.setForeground(color);
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            chatArea.setForeground(Color.BLACK);
        });
    }

    private void showMessage(String message) {
        chatArea.append("[System] " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatGUIClient::new);
    }
}

