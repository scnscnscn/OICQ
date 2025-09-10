package com.simpleqq.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.simpleqq.common.Message;
import com.simpleqq.common.MessageType;

/**
 * 私聊窗口类
 * 提供一对一聊天功能，支持文本消息和图片传输
 * 包含聊天记录加载、保存等功能
 */
public class SingleChatWindow extends JFrame {
    private final Client client;                    // 客户端连接对象
    private final String friendId;                  // 聊天对象的用户ID
    private JTextArea chatArea;               // 聊天内容显示区域
    private JTextField messageField;          // 消息输入框
    private JButton sendButton;               // 发送文本消息按钮
    private JButton sendImageButton;          // 发送图片按钮
    private JButton saveHistoryButton;        // 保存聊天记录按钮
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); // 时间格式化器

    /**
     * 构造函数
     * @param client 客户端对象
     * @param friendId 聊天对象的用户ID
     */
    public SingleChatWindow(Client client, String friendId) {
        this.client = client;
        this.friendId = friendId;
        
        initializeUI();
        setupEventHandlers();
        loadChatHistory();
        setupWindowCloseHandler();
    }

    /**
     * 初始化用户界面
     * 设置窗口布局和组件
     */
    private void initializeUI() {
        setTitle("与 " + friendId + " 聊天 - " + client.getCurrentUser().getUsername());
        setSize(500, 400);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        add(panel);

        // 创建聊天内容显示区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);        // 设置为只读
        chatArea.setLineWrap(true);         // 启用自动换行
        chatArea.setWrapStyleWord(true);    // 按单词换行
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // 创建输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("发送");
        sendImageButton = new JButton("发送图片");
        saveHistoryButton = new JButton("保存聊天记录");

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(sendButton);
        buttonPanel.add(sendImageButton);
        buttonPanel.add(saveHistoryButton);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * 设置事件处理器
     * 绑定按钮点击和键盘事件
     */
    private void setupEventHandlers() {
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage()); // 回车键发送消息
        sendImageButton.addActionListener(e -> sendImage());
        saveHistoryButton.addActionListener(e -> saveChatHistory());
    }

    /**
     * 发送文本消息
     * 获取输入框内容并发送给服务器
     */
    private void sendMessage() {
        String content = messageField.getText();
        if (!content.trim().isEmpty()) {
            // 创建文本消息对象
            Message message = new Message(MessageType.TEXT_MESSAGE, client.getCurrentUser().getId(), friendId, content);
            client.sendMessage(message);
            
            // 立即在界面显示自己发送的消息
            displayMessage(message);
            messageField.setText(""); // 清空输入框
        }
    }

    /**
     * 发送图片消息
     * 弹出文件选择对话框，选择图片并发送
     */
    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择图片");
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // 在后台线程中读取并编码大文件，避免阻塞 EDT
            sendImageButton.setEnabled(false);
            new Thread(() -> {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                    String imageContent = selectedFile.getName() + ":" + base64Image;
                    Message message = new Message(MessageType.IMAGE_MESSAGE, client.getCurrentUser().getId(), friendId, imageContent);
                    client.sendMessage(message);

                    Message displayMessage = new Message(MessageType.IMAGE_MESSAGE, client.getCurrentUser().getId(), friendId, selectedFile.getName());
                    SwingUtilities.invokeLater(() -> displayMessage(displayMessage));
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "发送图片失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
                } finally {
                    SwingUtilities.invokeLater(() -> sendImageButton.setEnabled(true));
                }
            }).start();
        }
    }

    /**
     * 显示消息到聊天区域
     * 处理文本消息和图片消息的显示
     * @param message 要显示的消息对象
     */
    public void displayMessage(Message message) {
        // 确定发送者显示名称
        String senderName = message.getSenderId().equals(client.getCurrentUser().getId()) ? "我" : message.getSenderId();
        String time = dateFormat.format(new Date(message.getTimestamp()));
        String displayContent;

        if (message.getType() == MessageType.IMAGE_MESSAGE) {
            // 处理图片消息显示
            String content = message.getContent();
            if (content.contains(":")) {
                // 包含图片数据的消息，提取文件名
                String fileName = content.split(":", 2)[0];
                displayContent = "[图片: " + fileName + "]";

                // 如果是接收到的图片消息，自动保存图片到本地（后台线程）
                if (!message.getSenderId().equals(client.getCurrentUser().getId())) {
                    String[] parts = content.split(":", 2);
                    if (parts.length == 2) {
                        String base64Image = parts[1];
                        chatArea.append(time + " [" + senderName + "]: " + displayContent + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());

                        new Thread(() -> {
                            try {
                                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                File saveDir = new File("received_images_from_" + message.getSenderId());
                                saveDir.mkdirs();
                                File outputFile = new File(saveDir, fileName);
                                Files.write(outputFile.toPath(), imageBytes);
                                String savedMsg = "(已保存到: " + outputFile.getAbsolutePath() + ")";
                                SwingUtilities.invokeLater(() -> {
                                    chatArea.append("    " + savedMsg + "\n");
                                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                                });
                            } catch (IOException ex) {
                                SwingUtilities.invokeLater(() -> {
                                    chatArea.append("    (保存图片失败: " + ex.getMessage() + ")\n");
                                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                                });
                            }
                        }).start();
                        return; // 已处理并返回
                    }
                }
            } else {
                // 只有文件名的消息
                displayContent = "[图片: " + content + "]";
            }
        } else {
            // 普通文本消息
            displayContent = message.getContent();
        }
        
        // 将消息添加到聊天区域
        chatArea.append(time + " [" + senderName + "]: " + displayContent + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 滚动到底部
    }

    /**
     * 加载聊天历史记录
     * 从本地文件读取之前的聊天记录并显示
     */
    private void loadChatHistory() {
        // 将历史记录的读取放到后台，避免在创建窗口时阻塞 EDT
        new Thread(() -> {
            String currentUserId = client.getCurrentUser().getId();
            String fileName;
            if (currentUserId.compareTo(friendId) < 0) {
                fileName = "chat_history_" + currentUserId + "_" + friendId + ".txt";
            } else {
                fileName = "chat_history_" + friendId + "_" + currentUserId + ".txt";
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String content = sb.toString();
                SwingUtilities.invokeLater(() -> {
                    chatArea.append(content);
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
            } catch (IOException e) {
                System.err.println("No chat history found for " + friendId + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * 保存聊天记录
     * 将当前聊天内容保存到用户指定的文件
     */
    private void saveChatHistory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存聊天记录");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // 设置默认文件名
        String defaultFileName = "chat_history_" + client.getCurrentUser().getId() + "_" + friendId + ".txt";
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                // 将聊天区域的所有文本保存到文件
                Files.write(fileToSave.toPath(), chatArea.getText().getBytes());
                JOptionPane.showMessageDialog(this, "聊天记录已保存到: " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存聊天记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 设置窗口关闭处理器
     * 窗口关闭时的清理工作
     */
    private void setupWindowCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口关闭时可以进行一些清理工作
                // 目前聊天记录由服务器自动保存，这里暂时不需要额外操作
            }
        });
    }
}