package com.simplechat.client;

import com.simplechat.common.Message;
import com.simplechat.common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private User currentUser;
    private Consumer<Message> messageListener;
    private boolean isConnected = false;
    private Thread readerThread;

    public Client() {
    }

    public void connect() throws IOException {
        try {
            System.out.println("Attempting to connect to server at " + SERVER_IP + ":" + SERVER_PORT);
            socket = new Socket(SERVER_IP, SERVER_PORT);
            socket.setKeepAlive(true);
            socket.setSoTimeout(0); // 无超时
            
            // 先创建输出流，再创建输入流
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush(); // 确保输出流被正确初始化
            ois = new ObjectInputStream(socket.getInputStream());
            
            isConnected = true;
            System.out.println("Connected to server successfully");

            // 启动消息接收线程
            readerThread = new Thread(() -> {
                try {
                    while (isConnected && !socket.isClosed() && socket.isConnected()) {
                        try {
                            Object obj = ois.readObject();
                            if (obj instanceof Message) {
                                Message message = (Message) obj;
                                System.out.println("Client received: " + message);
                                if (messageListener != null) {
                                    messageListener.accept(message);
                                }
                            }
                        } catch (SocketException e) {
                            if (isConnected) {
                                System.out.println("Server disconnected (socket closed)");
                            }
                            break;
                        } catch (IOException e) {
                            if (isConnected) {
                                System.out.println("Connection error: " + e.getMessage());
                            }
                            break;
                        } catch (ClassNotFoundException e) {
                            System.err.println("Invalid message format: " + e.getMessage());
                        }
                    }
                } finally {
                    if (isConnected) {
                        System.out.println("Message reader thread terminated");
                        disconnect();
                    }
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();
            
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            isConnected = false;
            cleanup();
            throw e;
        }
    }

    public void disconnect() {
        System.out.println("Disconnecting from server...");
        isConnected = false;
        cleanup();
    }

    private void cleanup() {
        try {
            if (oos != null) {
                oos.close();
                oos = null;
            }
            if (ois != null) {
                ois.close();
                ois = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        
        System.out.println("Client cleanup completed");
    }

    public void sendMessage(Message message) {
        if (!isConnected || socket == null || socket.isClosed()) {
            System.err.println("Not connected to server. Cannot send message.");
            return;
        }
        
        try {
            oos.writeObject(message);
            oos.flush();
            System.out.println("Sent message: " + message.getType());
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            // 连接出错时断开连接
            disconnect();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setMessageListener(Consumer<Message> listener) {
        this.messageListener = listener;
    }

    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed() && socket.isConnected();
    }
}