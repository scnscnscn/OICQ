package com.simpleqq.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.simpleqq.common.Message;
import com.simpleqq.common.MessageType;

public class Server {
    private static final int PORT = 8888;
    private UserManager userManager;
    private Map<String, ClientHandler> onlineClients;
    private boolean isRunning = false;
    
    public Server() {
        userManager = new UserManager();
        onlineClients = new ConcurrentHashMap<>();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public Map<String, ClientHandler> getOnlineClients() {
        return onlineClients;
    }

    public synchronized void addOnlineClient(String userId, ClientHandler handler) {
        onlineClients.put(userId, handler);
        System.out.println("User " + userId + " joined the chat room. Total online: " + onlineClients.size());
        
        // 通知所有用户有新用户加入
        broadcastMessage(new Message(MessageType.USER_JOIN, "Server", "ALL", userId + " joined the chat room"));
        
        // 发送在线用户列表给新用户
        sendUserList(handler);
        
        // 通知所有其他用户更新用户列表
        broadcastUserList();
    }

    public synchronized void removeClient(String userId) {
        if (userId != null) {
            onlineClients.remove(userId);
            System.out.println("User " + userId + " left the chat room. Total online: " + onlineClients.size());
            
            // 通知所有用户有用户离开
            broadcastMessage(new Message(MessageType.USER_LEAVE, "Server", "ALL", userId + " left the chat room"));
            
            // 通知所有用户更新用户列表
            broadcastUserList();
        }
    }

    public void broadcastMessage(Message message) {
        for (ClientHandler handler : onlineClients.values()) {
            try {
                if (handler != null) {
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
                // 移除无效的连接
                if (handler.getUserId() != null) {
                    onlineClients.remove(handler.getUserId());
                }
            }
        }
    }

    public void broadcastUserList() {
        StringBuilder userList = new StringBuilder();
        for (String userId : onlineClients.keySet()) {
            userList.append(userId).append(";");
        }
        if (userList.length() > 0) {
            userList.setLength(userList.length() - 1); // Remove trailing semicolon
        }
        
        Message userListMessage = new Message(MessageType.USER_LIST, "Server", "ALL", userList.toString());
        broadcastMessage(userListMessage);
    }

    private void sendUserList(ClientHandler handler) {
        StringBuilder userList = new StringBuilder();
        for (String userId : onlineClients.keySet()) {
            userList.append(userId).append(";");
        }
        if (userList.length() > 0) {
            userList.setLength(userList.length() - 1); // Remove trailing semicolon
        }
        
        try {
            if (handler != null && handler.getUserId() != null) {
                handler.sendMessage(new Message(MessageType.USER_LIST, "Server", handler.getUserId(), userList.toString()));
            }
        } catch (IOException e) {
            System.err.println("Error sending user list: " + e.getMessage());
        }
    }

    public void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Chat Server started successfully on port " + PORT);
            System.out.println("Waiting for client connections...");
            
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected from: " + clientSocket.getInetAddress().getHostAddress());
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    handler.start();
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + PORT + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        isRunning = false;
    }

    public static void main(String[] args) {
        System.out.println("Starting Chat Server...");
        Server server = new Server();
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));
        
        server.start();
    }
}