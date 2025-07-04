package com.simpleqq.client;

import com.simpleqq.common.Message;
import com.simpleqq.common.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class SingleChatWindow extends JFrame {
    private Client client;
    private String friendId;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton sendImageButton;
    private JButton saveHistoryButton; // New: Save chat history button
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private File selectedImageFile; // New: To store the selected image file

    public SingleChatWindow(Client client, String friendId) {
        this.client = client;
        this.friendId = friendId;

        setTitle("与 " + friendId + " 聊天 - " + client.getCurrentUser().getUsername());
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        add(panel);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("发送");
        sendImageButton = new JButton("发送图片");
        saveHistoryButton = new JButton("保存聊天记录"); // Initialize the new button

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3)); // Changed to 1,3 for new button
        buttonPanel.add(sendButton);
        buttonPanel.add(sendImageButton);
        buttonPanel.add(saveHistoryButton); // Add the new button

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        sendImageButton.addActionListener(e -> sendImage());
        saveHistoryButton.addActionListener(e -> saveChatHistory()); // Add action listener for save button

        loadChatHistory();

        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Optionally save chat history here, though server already saves
            }
        });
    }

    private void sendMessage() {
        String content = messageField.getText();
        if (!content.trim().isEmpty()) {
            Message message = new Message(MessageType.TEXT_MESSAGE, client.getCurrentUser().getId(), friendId, content);
            client.sendMessage(message);
            displayMessage(message); // Display own message immediately
            messageField.setText("");
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择图片");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            String fileName = selectedImageFile.getName();
            // Send IMAGE_REQUEST to the server
            Message imageRequest = new Message(MessageType.IMAGE_REQUEST, client.getCurrentUser().getId(), friendId, fileName);
            client.sendMessage(imageRequest);
            JOptionPane.showMessageDialog(this, "图片发送请求已发出，等待对方确认接收...");
        }
    }

    public void displayMessage(Message message) {
        String senderName = message.getSenderId().equals(client.getCurrentUser().getId()) ? "我" : message.getSenderId();
        String time = dateFormat.format(new Date(message.getTimestamp()));
        String displayContent;

        if (message.getType() == MessageType.IMAGE_MESSAGE) {
            // Display [图片: filename] in chat area
            displayContent = "[图片: " + message.getContent() + "]";
        } else {
            displayContent = message.getContent();
        }
        chatArea.append(time + " [" + senderName + "]: " + displayContent + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // Scroll to bottom
    }

    private void loadChatHistory() {
        String currentUserId = client.getCurrentUser().getId();
        String fileName;
        if (currentUserId.compareTo(friendId) < 0) {
            fileName = "chat_history_" + currentUserId + "_" + friendId + ".txt";
        } else {
            fileName = "chat_history_" + friendId + "_" + currentUserId + ".txt";
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                chatArea.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("No chat history found for " + friendId + ": " + e.getMessage());
        }
    }

    private void saveChatHistory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存聊天记录");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("chat_history_" + client.getCurrentUser().getId() + "_" + friendId + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                Files.write(fileToSave.toPath(), chatArea.getText().getBytes());
                JOptionPane.showMessageDialog(this, "聊天记录已保存到: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存聊天记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // New method to handle incoming image related messages
    public void handleImageMessage(Message message) {
        switch (message.getType()) {
            case IMAGE_REQUEST:
                String senderId = message.getSenderId();
                String fileName = message.getContent();
                int choice = JOptionPane.showConfirmDialog(this, senderId + " 想向您发送图片 " + fileName + "，是否接受？", "接收图片", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("选择图片保存位置");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int result = fileChooser.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File saveDirectory = fileChooser.getSelectedFile();
                        String savePath = saveDirectory.getAbsolutePath() + File.separator + fileName;
                        client.sendMessage(new Message(MessageType.IMAGE_ACCEPT, client.getCurrentUser().getId(), senderId, savePath));
                    } else {
                        client.sendMessage(new Message(MessageType.IMAGE_REJECT, client.getCurrentUser().getId(), senderId, "用户取消了保存。"));
                    }
                } else {
                    client.sendMessage(new Message(MessageType.IMAGE_REJECT, client.getCurrentUser().getId(), senderId, "用户拒绝接收图片。"));
                }
                break;
            case IMAGE_ACCEPT:
                // Receiver accepted, send the actual image data
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    // Content of IMAGE_ACCEPT message is the chosen save path by receiver
                    String savePathAndFileName = message.getContent(); // This now contains full path + filename
                    Message imageData = new Message(MessageType.IMAGE_DATA, client.getCurrentUser().getId(), friendId, savePathAndFileName + ":" + base64Image);
                    client.sendMessage(imageData);
                    displayMessage(new Message(MessageType.IMAGE_MESSAGE, client.getCurrentUser().getId(), friendId, selectedImageFile.getName())); // Display [图片] for sender
                    JOptionPane.showMessageDialog(this, "图片已发送并被对方接受。");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "读取图片失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case IMAGE_REJECT:
                JOptionPane.showMessageDialog(this, "对方拒绝接收您的图片: " + message.getContent());
                break;
            case IMAGE_DATA:
                // Receiver receives the actual image data
                String[] parts = message.getContent().split(":", 2); // Split into 2 parts: path+filename and base64
                if (parts.length == 2) {
                    String savePathAndFileName = parts[0];
                    String base64Image = parts[1];
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                        File outputFile = new File(savePathAndFileName);
                        // 确保父目录存在
                        if (outputFile.getParentFile() != null) {
                            outputFile.getParentFile().mkdirs();
                        }
                        Files.write(outputFile.toPath(), imageBytes);
                        displayMessage(new Message(MessageType.IMAGE_MESSAGE, message.getSenderId(), message.getReceiverId(), outputFile.getName())); // Display [图片] for receiver
                        JOptionPane.showMessageDialog(this, "已接收图片并保存到: " + outputFile.getAbsolutePath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "保存图片失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
        }
    }
}