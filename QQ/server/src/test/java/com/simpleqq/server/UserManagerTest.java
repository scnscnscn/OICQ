package com.simpleqq.server;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    public void rejectFriendRequest() {
        um.registerUser("r1", "R1", "p");
        um.registerUser("r2", "R2", "p");
        um.sendFriendRequest("r1", "r2");
        boolean rejected = um.rejectFriendRequest("r2", "r1");
        assertTrue(rejected);
        assertFalse(um.areFriends("r1", "r2"));
    }

    @Test
    public void getFriends() {
        um.registerUser("f1", "F1", "p");
        um.registerUser("f2", "F2", "p");
        um.registerUser("f3", "F3", "p");
        um.sendFriendRequest("f1", "f2");
        um.acceptFriendRequest("f2", "f1");
        um.sendFriendRequest("f1", "f3");
        um.acceptFriendRequest("f3", "f1");
        
        List<String> friends = um.getFriends("f1");
        assertTrue(friends.contains("f2"));
        assertTrue(friends.contains("f3"));
        assertEquals(2, friends.size());
    }

    @Test
    public void getPendingFriendRequests() {
        um.registerUser("p1", "P1", "p");
        um.registerUser("p2", "P2", "p");
        um.sendFriendRequest("p1", "p2");
        
        List<String> pending = um.getPendingFriendRequests("p2");
        assertTrue(pending.contains("p1"));
        assertEquals(1, pending.size());
    }

    @Test
    public void getUserById() {
        um.registerUser("u1", "User1", "pass");
        User user = um.getUserById("u1");
        assertNotNull(user);
        assertEquals("u1", user.getId());
        assertEquals("User1", user.getUsername());
    }

    @Test
    public void getAllUsers() {
        um.registerUser("all1", "All1", "p");
        um.registerUser("all2", "All2", "p");
        
        Map<String, User> allUsers = um.getAllUsers();
        assertTrue(allUsers.containsKey("all1"));
        assertTrue(allUsers.containsKey("all2"));
        assertEquals(2, allUsers.size());
    }

    @Test
    public void loginWithWrongPassword() {
        um.registerUser("wrong", "Wrong", "correct");
        User user = um.login("wrong", "incorrect");
        assertNull(user);
    }

    @Test
    public void loginNonExistentUser() {
        User user = um.login("nonexistent", "password");
        assertNull(user);
    }

    @Test
    public void sendFriendRequestToSelfFails() {
        um.registerUser("self", "Self", "p");
        boolean result = um.sendFriendRequest("self", "self");
        assertFalse(result);
    }

    @Test
    public void sendFriendRequestToNonExistentUserFails() {
        um.registerUser("exist", "Exist", "p");
        boolean result = um.sendFriendRequest("exist", "nonexistent");
        assertFalse(result);
    }

    @Test
    public void sendFriendRequestFromNonExistentUserFails() {
        um.registerUser("exist", "Exist", "p");
        boolean result = um.sendFriendRequest("nonexistent", "exist");
        assertFalse(result);
    }

    @Test
    public void acceptNonExistentFriendRequestFails() {
        um.registerUser("acc1", "Acc1", "p");
        um.registerUser("acc2", "Acc2", "p");
        boolean result = um.acceptFriendRequest("acc1", "acc2");
        assertFalse(result);
    }

    @Test
    public void rejectNonExistentFriendRequestFails() {
        um.registerUser("rej1", "Rej1", "p");
        um.registerUser("rej2", "Rej2", "p");
        boolean result = um.rejectFriendRequest("rej1", "rej2");
        assertFalse(result);
    }

    @Test
    public void deleteNonExistentFriendshipFails() {
        um.registerUser("del1", "Del1", "p");
        um.registerUser("del2", "Del2", "p");
        boolean result = um.deleteFriend("del1", "del2");
        assertFalse(result);
    }

    @Test
    public void areFriendsWithNonExistentUsers() {
        boolean result = um.areFriends("non1", "non2");
        assertFalse(result);
    }

    @Test
    public void getFriendsOfNonExistentUser() {
        List<String> friends = um.getFriends("nonexistent");
        assertTrue(friends.isEmpty());
    }

    @Test
    public void getPendingRequestsOfNonExistentUser() {
        List<String> requests = um.getPendingFriendRequests("nonexistent");
        assertTrue(requests.isEmpty());
    }

    @Test
    public void getNonExistentUser() {
        User user = um.getUserById("nonexistent");
        assertNull(user);
    }
}
