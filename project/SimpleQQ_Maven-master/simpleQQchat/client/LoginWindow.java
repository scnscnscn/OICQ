package com.simplechat.client;

import com.simplechat.common.Message;
import com.simplechat.common.MessageType;
import com.simplechat.common.User;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginWindow extends JFrame {
    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private Client client;

    public LoginWindow(Client client) {
        this.client = client;
        setTitle("简易聊天室 - 登录");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
        setupEventHandlers();
        
        // 设置消息监听器
        client.setMessageListener(this::handleMessage);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(panel);

        panel.add(new JLabel("用户ID:"));
        idField = new JTextField(15);
        panel.add(idField);

        panel.add(new JLabel("密码:"));
        passwordField = new JPasswordField(15);
        panel.add(passwordField);

        loginButton = new JButton("登录");
        panel.add(loginButton);

        registerButton = new JButton("注册");
        panel.add(registerButton);
    }

    private void setupEventHandlers() {
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin()); // 回车键登录
        
        registerButton.addActionListener(e -> {
            RegisterWindow registerWindow = new RegisterWindow(client, this);
            registerWindow.setVisible(true);
            this.setVisible(false);
        });
    }

    private void performLogin() {
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (id.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID和密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 禁用按钮防止重复点击
        setButtonsEnabled(false);
        
        // 在后台线程中处理连接
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (!client.isConnected()) {
                        System.out.println("Connecting to server...");
                        client.connect();
                        // 等待连接稳定
                        Thread.sleep(500);
                    }
                    
                    if (client.isConnected()) {
                        System.out.println("Sending login request...");
                        client.sendMessage(new Message(MessageType.LOGIN, id, "Server", id + "," + password));
                    } else {
                        throw new IOException("Failed to establish connection");
                    }
                } catch (Exception e) {
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // 检查是否有异常
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        String errorMsg = "连接服务器失败，请确保服务器已启动";
                        if (e.getCause() instanceof IOException) {
                            errorMsg += "\n详细信息: " + e.getCause().getMessage();
                        }
                        JOptionPane.showMessageDialog(LoginWindow.this, errorMsg, "连接错误", JOptionPane.ERROR_MESSAGE);
                        setButtonsEnabled(true);
                    });
                }
            }
        };
        worker.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        idField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }

    private void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case LOGIN_SUCCESS:
                    User loggedInUser = new User(message.getReceiverId(), message.getContent(), "");
                    loggedInUser.setOnline(true);
                    client.setCurrentUser(loggedInUser);
                    
                    ChatWindow chatWindow = new ChatWindow(client);
                    chatWindow.setVisible(true);
                    this.dispose();
                    break;
                    
                case LOGIN_FAIL:
                    JOptionPane.showMessageDialog(this, "登录失败: " + message.getContent(), 
                        "登录失败", JOptionPane.ERROR_MESSAGE);
                    setButtonsEnabled(true);
                    break;
                    
                default:
                    // 忽略其他消息类型
                    break;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println("Starting Chat Client...");
            Client client = new Client();
            new LoginWindow(client).setVisible(true);
        });
    }
}