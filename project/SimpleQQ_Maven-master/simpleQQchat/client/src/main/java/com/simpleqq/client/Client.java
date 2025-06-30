package com.simpleqq.client;

import com.simpleqq.common.Message;
import com.simpleqq.common.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class Client {
    private static final String SERVER_IP = "127.0.0.1"; // 服务器IP地址
    private static final int SERVER_PORT = 8888; // 服务器端口

    public Socket socket; 
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private User currentUser;

    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private Consumer<Message> messageListener;

    public Client() {
    }

    public void connect() throws IOException {
    }

    public void disconnect() {
    }

    public void sendMessage(Message message) {
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

