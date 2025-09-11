package com.simpleqq.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @Test
    public void messageFieldsAndToString() {
        Message m = new Message(MessageType.TEXT_MESSAGE, "s1", "r1", "hello");
        assertEquals(MessageType.TEXT_MESSAGE, m.getType());
        assertEquals("s1", m.getSenderId());
        assertEquals("r1", m.getReceiverId());
        assertEquals("hello", m.getContent());
        assertTrue(m.getTimestamp() > 0);
        String s = m.toString();
        assertTrue(s.contains("hello"));
        assertTrue(s.contains("s1"));
        assertTrue(s.contains("r1"));
    }
}
