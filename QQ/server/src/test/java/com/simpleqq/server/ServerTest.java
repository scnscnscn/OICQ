package com.simpleqq.server;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Server测试类
 * 测试服务器的核心功能，不包含网络监听部分
 */
public class ServerTest {

    private Server server;
    private java.nio.file.Path tempDir;

    @BeforeEach
    public void setup() throws Exception {
        tempDir = java.nio.file.Files.createTempDirectory("servertest");
        // 创建一个自定义的Server用于测试，使用临时目录存储数据文件
        server = new Server(tempDir.toString());
    }

    @AfterEach
    public void tearDown() throws Exception {
        // 清理临时文件和目录
        if (tempDir != null && java.nio.file.Files.exists(tempDir)) {
            // 递归删除目录中的所有文件和子目录
            try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(tempDir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            java.nio.file.Files.delete(path);
                        } catch (java.io.IOException e) {
                            System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
            }
        }
    }

    @Test
    public void testServerCreation() {
        assertNotNull(server);
        assertNotNull(server.getUserManager());
        assertNotNull(server.getGroupManager());
        assertNotNull(server.getOnlineClients());
    }

    @Test
    public void testUserManagerIntegration() {
        UserManager um = server.getUserManager();
        assertNotNull(um);

        // 测试用户注册
        boolean registered = um.registerUser("testuser", "Test User", "password");
        assertTrue(registered);

        // 测试用户登录
        var user = um.login("testuser", "password");
        assertNotNull(user);
        assertEquals("testuser", user.getId());
    }

    @Test
    public void testGroupManagerIntegration() {
        GroupManager gm = server.getGroupManager();
        assertNotNull(gm);

        // 测试群组创建
        boolean created = gm.createGroup("testgroup", "creator");
        assertTrue(created);

        // 测试获取群组成员
        var members = gm.getGroupMembers("testgroup");
        assertNotNull(members);
        assertTrue(members.contains("creator"));
    }

    @Test
    public void testOnlineClientsManagement() {
        Map<String, ClientHandler> onlineClients = server.getOnlineClients();
        assertNotNull(onlineClients);
        assertTrue(onlineClients.isEmpty());

        // 注意：实际添加客户端需要真正的ClientHandler实例
        // 这里我们只测试基本功能
    }

    @Test
    public void testIsUserOnline() {
        // 初始状态下没有在线用户
        assertFalse(server.isUserOnline("anyuser"));

        // 注意：实际测试需要模拟客户端连接
    }

    @Test
    public void testCreateGroup() {
        boolean result = server.createGroup("testgroup2", "creator2");
        assertTrue(result);

        // 验证群组是否真的被创建
        var members = server.getGroupManager().getGroupMembers("testgroup2");
        assertNotNull(members);
        assertTrue(members.contains("creator2"));
    }

    @Test
    public void testSaveChatMessage() {
        // 创建临时目录用于测试
        File testDir = new File(tempDir.toString(), ".history");
        testDir.mkdirs();

        // 注意：saveChatMessage方法会创建.history目录
        // 这里我们只测试方法调用不会抛出异常
        com.simpleqq.common.Message testMessage = new com.simpleqq.common.Message(
            com.simpleqq.common.MessageType.TEXT_MESSAGE, "sender", "receiver", "test content"
        );

        // 这个调用应该不会抛出异常
        assertDoesNotThrow(() -> server.saveChatMessage(testMessage));
    }

    @Test
    public void testSaveGroupMessage() {
        // 测试保存群组消息
        com.simpleqq.common.Message groupMessage = new com.simpleqq.common.Message(
            com.simpleqq.common.MessageType.GROUP_MESSAGE, "sender", "group1", "group message"
        );

        assertDoesNotThrow(() -> server.saveChatMessage(groupMessage));
    }

    @Test
    public void testSaveImageMessage() {
        // 测试保存图片消息
        com.simpleqq.common.Message imageMessage = new com.simpleqq.common.Message(
            com.simpleqq.common.MessageType.IMAGE_MESSAGE, "sender", "receiver", "image.jpg"
        );

        assertDoesNotThrow(() -> server.saveChatMessage(imageMessage));
    }

    @Test
    public void testForwardMessageWithNoReceiver() {
        // 测试转发消息给不存在的接收者
        com.simpleqq.common.Message message = new com.simpleqq.common.Message(
            com.simpleqq.common.MessageType.TEXT_MESSAGE, "sender", "offline_user", "test"
        );

        // 这个调用应该不会抛出异常，即使接收者不在线
        assertDoesNotThrow(() -> server.forwardMessage(message));
    }

    @Test
    public void testMultipleGroupCreation() {
        // 测试创建多个群组
        boolean result1 = server.createGroup("group1", "user1");
        boolean result2 = server.createGroup("group2", "user2");
        boolean result3 = server.createGroup("group1", "user3"); // 重复创建应该失败

        assertTrue(result1);
        assertTrue(result2);
        assertFalse(result3); // 重复创建失败
    }

    @Test
    public void testUserOnlineStatusAfterRegistration() {
        // 测试用户注册后的在线状态
        UserManager um = server.getUserManager();
        um.registerUser("onlinetest", "Online Test", "pass");

        // 初始状态应该是离线
        assertFalse(server.isUserOnline("onlinetest"));
    }

    @Test
    public void testEmptyOnlineClients() {
        // 测试初始状态下没有在线客户端
        Map<String, ClientHandler> clients = server.getOnlineClients();
        assertNotNull(clients);
        assertTrue(clients.isEmpty());
    }

    // TODO: 添加更多测试用例，特别是：
    // 1. 消息转发的测试（需要mock ClientHandler）
    // 2. 并发客户端管理的测试
    // 3. 异常情况的处理
    // 4. 聊天历史保存的详细测试
}
