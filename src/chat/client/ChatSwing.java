package chat.client;

import chat.common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ChatSwing extends JFrame {
    private JPanel messagePanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;

    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ChatSwing() {
        username = promptUsername();
        setupUI();
        connectToServer();
    }

    private String promptUsername() {
        return JOptionPane.showInputDialog(this, "Enter your username:", "Login", JOptionPane.PLAIN_MESSAGE);
    }

    private void setupUI() {
        setTitle("Group Chat - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);

        // Message panel for displaying messages
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.WHITE);

        // Scroll pane for the message panel
        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Input field and send button
        inputField = new JTextField();
        sendButton = new JButton("Send");

        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add listeners
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Layout everything
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(username); // send username to server

            // Start a listener thread
            Thread listener = new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        final String finalMsg = msg;
                        SwingUtilities.invokeLater(() -> addMessage(finalMsg, false));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> addMessage("[Disconnected from server]", false));
                }
            });

            listener.setDaemon(true);
            listener.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void addMessage(String text, boolean isOwnMessage) {
        JPanel wrapper = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("<html><p style='width: 300px;'>" + text + "</p></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBackground(isOwnMessage ? new Color(179, 229, 252) : new Color(220, 220, 220));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        if (isOwnMessage) {
            wrapper.add(messageLabel, BorderLayout.EAST);
        } else {
            wrapper.add(messageLabel, BorderLayout.WEST);
        }

        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messagePanel.add(wrapper);
        messagePanel.revalidate();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            Message formattedMessage = new Message(username, msg);
            addMessage(formattedMessage.format(), true);  // show locally
            out.println(msg);                             // send to server
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatSwing::new);
    }
}