package com.simpleqq.server;

import com.simpleqq.common.User;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static final String USERS_FILE = "users.txt";
    private Map<String, User> users;

    public UserManager() {
        users = new ConcurrentHashMap<>();
        loadUsers();
    }

    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    User user = new User(parts[0], parts[1], parts[2]);
                    users.put(user.getId(), user);
                }
            }
            System.out.println("Loaded " + users.size() + " users.");
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found. Creating a new one.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                writer.write(user.getId() + "|" + user.getUsername() + "|" + user.getPassword());
                writer.newLine();
            }
            System.out.println("Saved " + users.size() + " users.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean registerUser(String id, String username, String password) {
        if (users.containsKey(id)) {
            return false; // ID already exists
        }
        User newUser = new User(id, username, password);
        users.put(id, newUser);
        saveUsers();
        return true;
    }

    public synchronized User login(String id, String password) {
        User user = users.get(id);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getUserById(String id) {
        return users.get(id);
    }
}