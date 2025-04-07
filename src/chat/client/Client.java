package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Client{
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private Socket socket;

    public Client(String username, Socket socket){
        this.username = username;
        this.socket = socket;
        try{
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e){
            closeEverything();
        }
        buildGUI();
        sendUsername();
        listenForMessages();
    }
    private void buildGUI(){
        frame = new JFrame("Chat - " + username);
        chatArea = new JTextArea(20, 50);
        chatArea.setEditable(false);
        inputField = new JTextField(40);
        sendButton = new JButton("Send");

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(inputField);
        bottomPanel.add(sendButton);

        frame.getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }
    private void sendUsername(){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (IOException e) {
            closeEverything();
        }
    }
    private void sendMessage(){
        String message = inputField.getText();
        if(!message.isEmpty()){
            try{
                String fullMessage = username + ": " + message;
                bufferedWriter.write(fullMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                chatArea.append(fullMessage + "\n"); // 👈 Add this line
                inputField.setText("");
            }
            catch (IOException e){
                closeEverything();
            }
        }
    }
    private void listenForMessages(){
        new Thread(() -> {
            String msg;
            while (socket.isConnected()){
                try{
                    msg = bufferedReader.readLine();
                    if(msg != null){
                        chatArea.append(msg + "\n");
                    }
                }
                catch (IOException e){
                    closeEverything();
                    break;
                }
            }
        }).start();
    }
    private void closeEverything(){
        try{
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    private static boolean isValidLogin(String username, String password) {
        try{
            List<String> lines = Files.readAllLines(Paths.get("login.csv"));
            for(String line : lines.subList(1, lines.size())){
                String[] parts = line.split(",");
                if (parts.length == 2){
                    if (parts[0].equals(username) && parts[1].equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading login.csv");
        }
        return false;
    }
    public static void main(String[] args) {
        String username = null;
        String password = null;

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if(result == JOptionPane.OK_OPTION){
            username = userField.getText().trim();
            password = new String(passField.getPassword()).trim();

            if(!isValidLogin(username, password)){
                JOptionPane.showMessageDialog(null, "Invalid login. Exiting.");
                System.exit(0);
            }

            try{
                Socket socket = new Socket("localhost", 50000);
                String finalUsername = username;
                SwingUtilities.invokeLater(() -> new Client(finalUsername, socket));
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(null, "Unable to connect to server.");
            }
        }
    }
}
