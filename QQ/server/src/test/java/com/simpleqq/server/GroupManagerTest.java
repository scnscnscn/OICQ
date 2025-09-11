package com.simpleqq.server;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupManagerTest {

    private GroupManager gm;
    private java.nio.file.Path tempDir;

    @BeforeEach
    public void setup() {
        try {
            tempDir = java.nio.file.Files.createTempDirectory("gmtest");
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        gm = new GroupManager(tempDir.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(tempDir)) {
            files.forEach(p -> p.toFile().delete());
        }
        tempDir.toFile().delete();
    }

    @Test
    public void createGroupAndMembership() {
        boolean created = gm.createGroup("g1", "creator");
        assertTrue(created);
        List<String> members = gm.getGroupMembers("g1");
        assertNotNull(members);
        assertTrue(members.contains("creator"));
    }

    @Test
    public void inviteAndAcceptFlow() {
        gm.createGroup("g2", "owner");
        boolean invited = gm.sendGroupInvite("owner", "u2", "g2");
        assertTrue(invited);
        List<String> pending = gm.getPendingGroupInvites("u2");
        assertTrue(pending.contains("g2"));
        boolean accepted = gm.acceptGroupInvite("u2", "g2");
        assertTrue(accepted);
        assertTrue(gm.getGroupMembers("g2").contains("u2"));
    }
}
