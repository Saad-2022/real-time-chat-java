package chat.client;

import chat.common.Message;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatSwing extends JFrame {
    private GradientPanel avatarPanel;
    private GradientPanel messagePanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;

    private Map<String, JPanel> avatarMap = new HashMap<>();
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    // Modern font for UI elements
    private final Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);

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
        setSize(600, 500);
        setLocationRelativeTo(null);

        // Avatar panel with gradient background
        avatarPanel = new GradientPanel(new Color(255, 255, 255), new Color(240, 240, 240));
        avatarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        avatarPanel.setPreferredSize(new Dimension(0, 60));
        avatarPanel.setBorder(BorderFactory.createTitledBorder("Users Online:"));

        // Message panel with gradient background
        messagePanel = new GradientPanel(new Color(250, 250, 250), new Color(230, 230, 230));
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(avatarPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Input field, emoji button, and send button panel
        inputField = new JTextField();
        inputField.setFont(modernFont);

        sendButton = new JButton("Send");
        sendButton.setFont(modernFont);
        sendButton.setBorder(new RoundedBorder(10));

        JButton emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        emojiButton.setFocusPainted(false);
        emojiButton.addActionListener(e -> showEmojiPicker());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(emojiButton, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(username); // send username to the server

            Thread listener = new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        final String finalMsg = msg;
                        SwingUtilities.invokeLater(() -> {
                            if (finalMsg.startsWith("JOIN ")) {
                                String user = finalMsg.substring(5);
                                addUserAvatar(user);
                            } else if (finalMsg.startsWith("LEAVE ")) {
                                String user = finalMsg.substring(6);
                                removeUserAvatar(user);
                            } else {
                                // Avoid duplicating your own message
                                if (!finalMsg.startsWith(username + " [")) {
                                    addMessage(finalMsg, false);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> addMessage("[Disconnected from server]", false));
                }
            });
            listener.setDaemon(true);
            listener.start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            Message formatted = new Message(username, msg);
            addMessage(formatted.format(), true);
            out.println(msg);
            inputField.setText("");
        }
    }

    private void addMessage(String text, boolean isOwnMessage) {
        JPanel bubbleWrapper = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT));
        bubbleWrapper.setOpaque(false);
        bubbleWrapper.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel bubble = new JLabel("<html><p style='width: 300px;'>" + text + "</p></html>");
        bubble.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        bubble.setOpaque(true);
        bubble.setBackground(isOwnMessage ? new Color(179, 229, 252) : new Color(230, 230, 230));
        bubble.setForeground(Color.BLACK);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(15),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel reactionLabel = new JLabel(" ");
        reactionLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        // Reaction picker on bubble click
        bubble.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String[] reactions = {"❤️", "😂", "👍", "🔥", "😢", "😮"};
                JPopupMenu reactionMenu = new JPopupMenu();
                for (String emoji : reactions) {
                    JMenuItem item = new JMenuItem(emoji);
                    item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                    item.addActionListener(ev -> reactionLabel.setText(emoji));
                    reactionMenu.add(item);
                }
                reactionMenu.show(bubble, e.getX(), e.getY());
            }
        });

        JPanel messageWithReaction = new JPanel(new BorderLayout());
        messageWithReaction.setOpaque(false);
        messageWithReaction.add(bubble, BorderLayout.CENTER);
        messageWithReaction.add(reactionLabel, isOwnMessage ? BorderLayout.EAST : BorderLayout.WEST);

        bubbleWrapper.add(messageWithReaction);
        messagePanel.add(bubbleWrapper);
        messagePanel.revalidate();
        messagePanel.repaint();

        // Optional fade-in animation for new messages
        animateMessage(bubbleWrapper);

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum())
        );
    }

    private void animateMessage(JPanel panel) {
        Timer timer = new Timer(30, null);
        final float[] alpha = {0.0f};
        timer.addActionListener(e -> {
            alpha[0] += 0.1f;
            // This is a simple demonstration; for a real fade-in, you might override paintComponent.
            panel.setBackground(new Color(1.0f, 1.0f, 1.0f, Math.min(alpha[0], 1.0f)));
            panel.repaint();
            if (alpha[0] >= 1.0f) {
                timer.stop();
            }
        });
        timer.start();
    }

    private void addUserAvatar(String user) {
        if (user.equals(this.username) || avatarMap.containsKey(user)) return;

        String initials = getInitials(user);
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(30, 30));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(100, 149, 237));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Arial", Font.BOLD, 12));
        avatar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1, true));
        avatar.setToolTipText(user);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(avatar, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(35, 35));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        avatarPanel.add(wrapper);
        avatarMap.put(user, wrapper);
        avatarPanel.revalidate();
        avatarPanel.repaint();
    }

    private void removeUserAvatar(String user) {
        JPanel wrapper = avatarMap.remove(user);
        if (wrapper != null) {
            avatarPanel.remove(wrapper);
            avatarPanel.revalidate();
            avatarPanel.repaint();
        }
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void showEmojiPicker() {
        JDialog emojiDialog = new JDialog(this, "Pick an Emoji", false);
        emojiDialog.setSize(300, 200);
        emojiDialog.setLayout(new GridLayout(5, 6, 5, 5));

        String[] emojis = {
                "😀", "😂", "😍", "😎", "😭", "😊",
                "👍", "🔥", "💯", "🎉", "😢", "😡",
                "🙌", "🤔", "💀", "🤯", "😴", "👀",
                "👋", "👏", "😇", "🤡", "😱", "❤️",
                "🤖", "☕", "🍕", "🐶", "🚀", "🌟"
        };

        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            emojiBtn.setFocusPainted(false);
            emojiBtn.addActionListener(e -> {
                inputField.setText(inputField.getText() + emoji);
                emojiDialog.dispose();
                inputField.requestFocus();
            });
            emojiDialog.add(emojiBtn);
        }

        emojiDialog.setLocationRelativeTo(this);
        emojiDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatSwing::new);
    }

    // Custom gradient panel for a modern background look
    class GradientPanel extends JPanel {
        private Color startColor;
        private Color endColor;

        public GradientPanel(Color start, Color end) {
            this.startColor = start;
            this.endColor = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, startColor, w, h, endColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            super.paintComponent(g);
        }
    }

    // Rounded border class for a sleek, modern look
    class RoundedBorder implements Border {
        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 2, radius);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.GRAY);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
