package com.simpleqq.server;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.simpleqq.common.User;

public class UserManagerTest {

    private UserManager um;
    private java.nio.file.Path tempDir;

    @BeforeEach
    public void setup() {
        try {
            tempDir = java.nio.file.Files.createTempDirectory("umtest");
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        um = new UserManager(tempDir.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        // try to delete temp files created during tests
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(tempDir)) {
            files.forEach(p -> p.toFile().delete());
        }
        tempDir.toFile().delete();
    }

    @Test
    public void registerAndLogin() {
        boolean r = um.registerUser("u100", "TestUser", "pwd");
        assertTrue(r);
        User u = um.login("u100", "pwd");
        assertNotNull(u);
        assertEquals("u100", u.getId());
    }

    @Test
    public void preventDuplicateRegister() {
        um.registerUser("u101", "A", "1");
        boolean r2 = um.registerUser("u101", "A", "1");
        assertFalse(r2);
    }

    @Test
    public void friendRequestFlow() {
        um.registerUser("s1", "S", "p");
        um.registerUser("r1", "R", "p");
        boolean sent = um.sendFriendRequest("s1", "r1");
        assertTrue(sent);
        List<String> pending = um.getPendingFriendRequests("r1");
        assertTrue(pending.contains("s1"));
        boolean accepted = um.acceptFriendRequest("r1", "s1");
        assertTrue(accepted);
        assertTrue(um.areFriends("s1", "r1"));
    }

    @Test
    public void deleteFriendWorks() {
        um.registerUser("a1", "A1", "p");
        um.registerUser("a2", "A2", "p");
        um.sendFriendRequest("a1", "a2");
        um.acceptFriendRequest("a2", "a1");
        assertTrue(um.areFriends("a1", "a2"));
        boolean del = um.deleteFriend("a1", "a2");
        assertTrue(del);
        assertFalse(um.areFriends("a1", "a2"));
    }
}
