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

    @Test
    public void rejectGroupInvite() {
        gm.createGroup("g3", "owner");
        gm.sendGroupInvite("owner", "u3", "g3");
        boolean rejected = gm.rejectGroupInvite("u3", "g3");
        assertTrue(rejected);
        assertFalse(gm.getPendingGroupInvites("u3").contains("g3"));
    }

    @Test
    public void getUserGroups() {
        gm.createGroup("g4", "user1");
        gm.createGroup("g5", "user1");
        gm.createGroup("g6", "user2");
        
        List<String> user1Groups = gm.getUserGroups("user1");
        assertTrue(user1Groups.contains("g4"));
        assertTrue(user1Groups.contains("g5"));
        assertFalse(user1Groups.contains("g6"));
        
        List<String> user2Groups = gm.getUserGroups("user2");
        assertTrue(user2Groups.contains("g6"));
        assertFalse(user2Groups.contains("g4"));
    }

    @Test
    public void getAllGroups() {
        gm.createGroup("g7", "user1");
        gm.createGroup("g8", "user2");
        
        Map<String, List<String>> allGroups = gm.getAllGroups();
        assertTrue(allGroups.containsKey("g7"));
        assertTrue(allGroups.containsKey("g8"));
        assertEquals(2, allGroups.size());
    }

    @Test
    public void createDuplicateGroupFails() {
        gm.createGroup("g9", "user1");
        boolean result = gm.createGroup("g9", "user2");
        assertFalse(result);
    }

    @Test
    public void sendInviteToNonExistentGroupFails() {
        boolean result = gm.sendGroupInvite("user1", "user2", "nonexistent");
        assertFalse(result);
    }

    @Test
    public void sendInviteToExistingMemberFails() {
        gm.createGroup("g10", "user1");
        gm.sendGroupInvite("user1", "user2", "g10");
        gm.acceptGroupInvite("user2", "g10");
        
        boolean result = gm.sendGroupInvite("user1", "user2", "g10");
        assertFalse(result);
    }

    @Test
    public void acceptNonExistentInviteFails() {
        boolean result = gm.acceptGroupInvite("user1", "nonexistent");
        assertFalse(result);
    }

    @Test
    public void rejectNonExistentInviteFails() {
        boolean result = gm.rejectGroupInvite("user1", "nonexistent");
        assertFalse(result);
    }

    @Test
    public void getMembersOfNonExistentGroup() {
        List<String> members = gm.getGroupMembers("nonexistent");
        assertNull(members);
    }

    @Test
    public void getPendingInvitesForNonExistentUser() {
        List<String> invites = gm.getPendingGroupInvites("nonexistent");
        assertTrue(invites.isEmpty());
    }
}
