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
    // 创建按钮面板
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.add(sendButton);
    buttonPanel.add(sendImageButton);

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
            File historyFile = findExistingHistoryFile(fileName);

            try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
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
                System.err.println("No chat history found for " + friendId + " at " + historyFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }).start();
    }

    // 查找已存在的历史文件：优先使用当前运行目录下的 .history，其次向上查找父目录中的 .history（最多3层），
    // 并尝试查找 ancestor/server/.history（兼容 server 模块保存位置）。如果都没找到，返回当前运行目录下的目标路径（并创建 .history）。
    private File findExistingHistoryFile(String fileName) {
        File cwd = new File(System.getProperty("user.dir"));
        File tryDir = new File(cwd, ".history");
        File tryFile = new File(tryDir, fileName);
        if (tryFile.exists()) {
            System.out.println("Found history at: " + tryFile.getAbsolutePath());
            return tryFile;
        }

        // 向上查找父目录的 .history（最多3层）以及可能的 server/.history
        File dir = cwd;
        for (int i = 0; i < 3; i++) {
            dir = dir.getParentFile();
            if (dir == null) break;
            File parentHistory = new File(dir, ".history");
            File parentFile = new File(parentHistory, fileName);
            if (parentFile.exists()) {
                System.out.println("Found history at parent: " + parentFile.getAbsolutePath());
                return parentFile;
            }
            // 检查可能的 server/.history
            File serverHistory = new File(dir, "server/.history");
            File serverFile = new File(serverHistory, fileName);
            if (serverFile.exists()) {
                System.out.println("Found history at server: " + serverFile.getAbsolutePath());
                return serverFile;
            }
        }

        // 再次从当前目录向上彻底查找 server/.history（直到根）
        File cur = cwd;
        while (cur != null) {
            File serverHistory = new File(cur, "server/.history");
            File serverFile = new File(serverHistory, fileName);
            if (serverFile.exists()) {
                System.out.println("Found history at ancestor server: " + serverFile.getAbsolutePath());
                return serverFile;
            }
            cur = cur.getParentFile();
        }

        // 如果都没找到，确保当前运行目录下的 .history 存在并返回默认路径
        if (!tryDir.exists()) tryDir.mkdirs();
        System.out.println("No existing history found; will try: " + tryFile.getAbsolutePath());
        return tryFile;
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