package com.simpleqq.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void userFieldsAndOnlineFlag() {
        User u = new User("u1", "Alice", "pass");
        assertEquals("u1", u.getId());
        assertEquals("Alice", u.getUsername());
        assertEquals("pass", u.getPassword());
        assertFalse(u.isOnline());
        u.setOnline(true);
        assertTrue(u.isOnline());
    }

    @Test
    public void userToStringContainsFields() {
        User u = new User("u2", "Bob", "pwd");
        String s = u.toString();
        assertTrue(s.contains("u2"));
        assertTrue(s.contains("Bob"));
        assertTrue(s.contains("pwd"));
    }
}
