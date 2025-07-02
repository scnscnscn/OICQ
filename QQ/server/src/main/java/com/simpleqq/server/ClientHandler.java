package com.simpleqq.server;

import com.simpleqq.common.Message;
import com.simpleqq.common.MessageType;
import com.simpleqq.common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {
    private Socket socket;
    private Server server;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String userId;
    private boolean isRunning = false;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            // 设置socket选项
            socket.setKeepAlive(true);
            socket.setSoTimeout(0); // 无超时
            
            // 先创建输出流，再创建输入流
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush(); // 确保输出流被正确初始化
            ois = new ObjectInputStream(socket.getInputStream());
            
            isRunning = true;
            System.out.println("Client handler initialized successfully for: " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Error initializing client handler: " + e.getMessage());
            e.printStackTrace();
            cleanup();
        }
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public void run() {
        System.out.println("Client handler thread started for: " + socket.getInetAddress());
        
        try {
            while (isRunning && !socket.isClosed() && socket.isConnected()) {
                try {
                    Object obj = ois.readObject();
                    if (obj instanceof Message) {
                        Message message = (Message) obj;
                        System.out.println("Received message from client " + 
                            (userId != null ? userId : "unknown") + ": " + message);
                        handleMessage(message);
                    }
                } catch (SocketException e) {
                    System.out.println("Client " + (userId != null ? userId : "unknown") + 
                        " disconnected (socket closed)");
                    break;
                } catch (IOException e) {
                    if (isRunning) {
                        System.out.println("Client " + (userId != null ? userId : "unknown") + 
                            " disconnected: " + e.getMessage());
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Invalid message format from client: " + e.getMessage());
                }
            }
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message message) {
        try {
            switch (message.getType()) {
                case LOGIN:
                    handleLogin(message);
                    break;
                case REGISTER:
                    handleRegister(message);
                    break;
                case CHAT_MESSAGE:
                    handleChatMessage(message);
                    break;
                default:
                    System.out.println("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanup() {
        isRunning = false;
        if (userId != null) {
            server.removeClient(userId);
        }
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        System.out.println("Client handler cleanup completed for user: " + 
            (userId != null ? userId : "unknown"));
    }

    private void handleLogin(Message message) {
        try {
            String content = message.getContent();
            if (content == null || content.trim().isEmpty()) {
                sendMessage(new Message(MessageType.LOGIN_FAIL, "Server", 
                    message.getSenderId(), "Empty credentials."));
                return;
            }
            
            String[] credentials = content.split(",");
            if (credentials.length != 2) {
                sendMessage(new Message(MessageType.LOGIN_FAIL, "Server", 
                    message.getSenderId(), "Invalid credentials format."));
                return;
            }
            
            String id = credentials[0].trim();
            String password = credentials[1].trim();
            
            if (id.isEmpty() || password.isEmpty()) {
                sendMessage(new Message(MessageType.LOGIN_FAIL, "Server", 
                    message.getSenderId(), "ID and password cannot be empty."));
                return;
            }
            
            User user = server.getUserManager().login(id, password);

            if (user != null) {
                if (server.getOnlineClients().containsKey(id)) {
                    sendMessage(new Message(MessageType.LOGIN_FAIL, "Server", id, 
                        "User already online."));
                    return;
                }
                this.userId = id;
                server.addOnlineClient(id, this);
                sendMessage(new Message(MessageType.LOGIN_SUCCESS, "Server", id, user.getUsername()));
                System.out.println("User " + id + " logged in successfully");
            } else {
                sendMessage(new Message(MessageType.LOGIN_FAIL, "Server", 
                    message.getSenderId(), "Invalid ID or password."));
                System.out.println("Failed login attempt for ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error handling login: " + e.getMessage());
            e.printStackTrace();
            try {
                sendMessage(new Message(MessageType.LOGIN_FAIL, "Server", 
                    message.getSenderId(), "Server error during login."));
            } catch (IOException ioException) {
                System.err.println("Failed to send error message: " + ioException.getMessage());
            }
        }
    }

    private void handleRegister(Message message) {
        try {
            String content = message.getContent();
            if (content == null || content.trim().isEmpty()) {
                sendMessage(new Message(MessageType.REGISTER_FAIL, "Server", 
                    message.getSenderId(), "Empty registration data."));
                return;
            }
            
            String[] userInfo = content.split(",");
            if (userInfo.length != 3) {
                sendMessage(new Message(MessageType.REGISTER_FAIL, "Server", 
                    message.getSenderId(), "Invalid registration format."));
                return;
            }
            
            String id = userInfo[0].trim();
            String username = userInfo[1].trim();
            String password = userInfo[2].trim();
            
            if (id.isEmpty() || username.isEmpty() || password.isEmpty()) {
                sendMessage(new Message(MessageType.REGISTER_FAIL, "Server", 
                    message.getSenderId(), "All fields are required."));
                return;
            }

            if (server.getUserManager().registerUser(id, username, password)) {
                sendMessage(new Message(MessageType.REGISTER_SUCCESS, "Server", id, 
                    "Registration successful."));
                System.out.println("User " + id + " registered successfully");
            } else {
                sendMessage(new Message(MessageType.REGISTER_FAIL, "Server", id, 
                    "ID already exists."));
                System.out.println("Registration failed for ID: " + id + " (already exists)");
            }
        } catch (Exception e) {
            System.err.println("Error handling registration: " + e.getMessage());
            e.printStackTrace();
            try {
                sendMessage(new Message(MessageType.REGISTER_FAIL, "Server", 
                    message.getSenderId(), "Server error during registration."));
            } catch (IOException ioException) {
                System.err.println("Failed to send error message: " + ioException.getMessage());
            }
        }
    }

    private void handleChatMessage(Message message) {
        try {
            System.out.println("Broadcasting chat message from " + message.getSenderId());
            // 广播聊天消息给所有在线用户（除了发送者）
            for (ClientHandler handler : server.getOnlineClients().values()) {
                if (handler != null && !handler.getUserId().equals(message.getSenderId())) {
                    try {
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        System.err.println("Error forwarding message to " + handler.getUserId() + ": " + e.getMessage());
                        // 移除无效连接
                        server.getOnlineClients().remove(handler.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling chat message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) throws IOException {
        if (oos != null && !socket.isClosed() && socket.isConnected()) {
            try {
                oos.writeObject(message);
                oos.flush();
                System.out.println("Sent message to " + (userId != null ? userId : "unknown") + ": " + message.getType());
            } catch (IOException e) {
                System.err.println("Failed to send message to " + (userId != null ? userId : "unknown") + ": " + e.getMessage());
                throw e;
            }
        } else {
            throw new IOException("Connection is closed or invalid");
        }
    }
}