package com.simplechat.client;

import com.simplechat.common.Message;
import com.simplechat.common.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatWindow extends JFrame {
    private Client client;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public ChatWindow(Client client) {
        this.client = client;
        
        setTitle("简易聊天室 - " + client.getCurrentUser().getUsername() + " (" + client.getCurrentUser().getId() + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        // 设置消息监听器
        client.setMessageListener(this::handleIncomingMessage);
    }

    private void initializeComponents() {
        // 聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 消息输入框
        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 在线用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 主聊天面板
        JPanel chatPanel = new JPanel(new BorderLayout());
        
        // 聊天显示区域
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        // 右侧用户列表面板
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(200, 0));
        userPanel.setBorder(BorderFactory.createTitledBorder("在线用户"));
        
        JScrollPane userScrollPane = new JScrollPane(userList);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        
        add(userPanel, BorderLayout.EAST);
    }

    private void setupEventHandlers() {
        // 发送按钮事件
        sendButton.addActionListener(e -> sendMessage());
        
        // 回车发送消息
        messageField.addActionListener(e -> sendMessage());
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
                System.exit(0);
            }
        });
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (!content.isEmpty()) {
            Message message = new Message(MessageType.CHAT_MESSAGE, 
                                        client.getCurrentUser().getId(), 
                                        "ALL", 
                                        content);
            client.sendMessage(message);
            
            // 在聊天区域显示自己的消息
            displayMessage(client.getCurrentUser().getId(), content, true);
            messageField.setText("");
        }
    }

    private void handleIncomingMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case CHAT_MESSAGE:
                    displayMessage(message.getSenderId(), message.getContent(), false);
                    break;
                case USER_LIST:
                    updateUserList(message.getContent());
                    break;
                case USER_JOIN:
                    displaySystemMessage(message.getContent());
                    break;
                case USER_LEAVE:
                    displaySystemMessage(message.getContent());
                    break;
                default:
                    System.out.println("Unhandled message type: " + message.getType());
            }
        });
    }

    private void displayMessage(String senderId, String content, boolean isOwnMessage) {
        String time = dateFormat.format(new Date());
        String senderName = isOwnMessage ? "我" : senderId;
        String messageText = time + " [" + senderName + "]: " + content + "\n";
        
        chatArea.append(messageText);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void displaySystemMessage(String content) {
        String time = dateFormat.format(new Date());
        String messageText = time + " [系统]: " + content + "\n";
        
        chatArea.append(messageText);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void updateUserList(String userListStr) {
        userListModel.clear();
        if (userListStr != null && !userListStr.isEmpty()) {
            String[] users = userListStr.split(";");
            for (String userId : users) {
                userListModel.addElement(userId);
            }
        }
    }
}