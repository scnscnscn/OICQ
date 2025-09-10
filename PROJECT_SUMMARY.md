# SimpleQQ 即时通讯系统 - 项目总结

## 3.2 所有功能模块描述

### 3.2.1 模块简介

本项目是一个基于Java的即时通讯系统，采用经典的C/S架构设计，主要分为以下三个核心模块：

#### 1. Common模块（公共模块）
- **功能**：定义系统中所有通用的数据结构和消息协议
- **核心组件**：
  - `User`：用户实体类，封装用户基本信息
  - `Message`：消息实体类，定义消息传输格式
  - `MessageType`：消息类型枚举，规范通信协议
- **特点**：实现序列化接口，支持网络传输

#### 2. Server模块（服务器端模块）
- **功能**：处理客户端连接、消息转发、数据持久化
- **核心组件**：
  - `Server`：服务器主类，管理连接和消息分发
  - `ClientHandler`：客户端处理器，独立线程处理单个客户端
  - `UserManager`：用户管理器，处理用户注册登录和好友关系
  - `GroupManager`：群组管理器，处理群组创建和成员管理
- **特点**：多线程并发处理，文件系统持久化

#### 3. Client模块（客户端模块）
- **功能**：提供用户交互界面，处理用户操作
- **核心组件**：
  - `LoginWindow`：登录注册界面
  - `ChatWindow`：主聊天界面，好友和群组管理
  - `SingleChatWindow`：私聊窗口
  - `GroupChatWindow`：群聊窗口
  - `RegisterWindow`：用户注册界面
  - `Client`：客户端核心类，网络通信处理
- **特点**：基于Swing的图形界面，事件驱动设计

### 3.2.2 模块框架设计以及处理流程

#### 1. 系统整体架构

```
┌─────────────────┐    TCP Socket    ┌─────────────────┐
│   Client Side   │ ←─────────────→  │   Server Side   │
│                 │                  │                 │
│  ┌─────────────┐│                  │┌─────────────┐  │
│  │  UI Layer   ││                  ││   Server    │  │
│  │ (Swing GUI) ││                  ││   Manager   │  │
│  └─────────────┘│                  │└─────────────┘  │
│  ┌─────────────┐│                  │┌─────────────┐  │
│  │   Client    ││                  ││ ClientHandler│  │
│  │  Network    ││                  ││  (Thread)   │  │
│  │   Layer     ││                  │└─────────────┘  │
│  └─────────────┘│                  │┌─────────────┐  │
│  ┌─────────────┐│                  ││UserManager/ │  │
│  │   Common    ││                  ││GroupManager │  │
│  │  Messages   ││                  │└─────────────┘  │
│  └─────────────┘│                  │┌─────────────┐  │
└─────────────────┘                  ││ File Storage│  │
                                     │└─────────────┘  │
                                     └─────────────────┘
```

#### 2. 服务器端处理流程

##### 启动流程：
1. **初始化阶段**：
   - 创建UserManager和GroupManager实例
   - 从文件系统加载用户数据、好友关系、群组信息
   - 初始化在线客户端管理Map
   - 创建ServerSocket监听指定端口(8888)

2. **连接处理阶段**：
   - 服务器持续监听新的客户端连接
   - 接受连接后创建新的ClientHandler线程
   - 为每个客户端分配独立的处理线程

##### 消息处理流程：
```
客户端消息 → ObjectInputStream → 消息解析 → 类型分发
                                              ↓
┌─────────────────────────────────────────────────────────┐
│  消息类型分发处理：                                           │
│  • LOGIN/REGISTER → 用户认证处理                          │
│  • FRIEND_* → 好友关系处理                               │
│  • TEXT/IMAGE_MESSAGE → 消息转发                        │
│  • GROUP_* → 群组管理处理                                │
│  • CREATE_GROUP → 群组创建                              │
└─────────────────────────────────────────────────────────┘
                                              ↓
                                        响应消息构建
                                              ↓
                                   ObjectOutputStream → 客户端
                                              ↓
                                        数据持久化存储
```

#### 3. 客户端处理流程

##### 启动流程：
1. **连接建立**：
   - 创建Socket连接到服务器
   - 建立对象输入输出流
   - 启动消息监听线程

2. **用户认证**：
   - 显示登录界面
   - 处理用户输入验证
   - 发送认证请求到服务器
   - 根据响应结果跳转界面

##### 界面交互流程：
```
用户操作 → UI事件触发 → 事件监听器处理 → 消息构建
                                              ↓
                                      网络发送到服务器
                                              ↓
                                      服务器响应接收
                                              ↓
                                      界面更新/窗口操作
```

### 3.2.3 接口设计

#### 1. 网络通信接口

##### 消息传输协议：
```java
public class Message implements Serializable {
    private MessageType type;      // 消息类型
    private String senderId;       // 发送者ID
    private String receiverId;     // 接收者ID
    private long timestamp;        // 时间戳
    private String content;        // 消息内容
}
```

##### 主要消息类型：
- **用户认证类**：`LOGIN`, `REGISTER`, `LOGIN_SUCCESS`, `LOGIN_FAIL`
- **消息传输类**：`TEXT_MESSAGE`, `IMAGE_MESSAGE`, `GROUP_MESSAGE`
- **好友管理类**：`FRIEND_REQUEST`, `FRIEND_ACCEPT`, `FRIEND_REJECT`
- **群组管理类**：`CREATE_GROUP`, `GROUP_INVITE`, `GROUP_ACCEPT`

#### 2. 服务器管理接口

##### UserManager接口功能：
```java
// 用户认证
public User login(String id, String password)
public boolean register(User user)

// 好友管理
public boolean sendFriendRequest(String fromUserId, String toUserId)
public boolean acceptFriendRequest(String fromUserId, String toUserId)
public List<String> getFriends(String userId)

// 数据持久化
private void saveUsers()
private void saveFriendships()
```

##### GroupManager接口功能：
```java
// 群组管理
public boolean createGroup(String groupId, String creatorId)
public boolean addMemberToGroup(String groupId, String userId)
public List<String> getGroupMembers(String groupId)

// 群组邀请
public boolean inviteToGroup(String groupId, String inviterId, String inviteeId)
public boolean acceptGroupInvite(String groupId, String userId)
```

#### 3. 客户端界面接口

##### 窗口管理接口：
```java
// 主窗口接口
public interface ChatWindowInterface {
    void refreshFriendList();
    void refreshGroupList();
    void openSingleChat(String friendId);
    void openGroupChat(String groupId);
}

// 聊天窗口接口
public interface ChatInterface {
    void sendMessage(String message);
    void sendImage(String imagePath);
    void displayMessage(String sender, String message, long timestamp);
    void exportChatHistory();
}
```

## 4.2 数据结构设计

### 4.2.1 逻辑结构设计

#### 1. 用户关系模型

```
用户实体 (User)
├── id: String (主键，唯一标识)
├── username: String (显示名称)
├── password: String (登录凭据)
└── isOnline: boolean (在线状态)

好友关系 (Friendship)
├── 双向关系：User ←→ User
├── 关系状态：pending（待确认）、accepted（已确认）
└── 存储结构：Map<String, List<String>>
```

#### 2. 群组关系模型

```
群组实体 (Group)
├── groupId: String (群组唯一标识)
├── members: List<String> (成员ID列表)
├── creator: String (创建者ID)
└── createdTime: long (创建时间)

群组关系结构：
├── 一对多关系：Group → Users
├── 成员权限：创建者具有管理权限
└── 存储结构：Map<String, List<String>>
```

#### 3. 消息存储模型

```
消息实体 (Message)
├── 消息类型：text、image、system
├── 关联关系：sender → receiver
├── 时序关系：timestamp排序
└── 分类存储：私聊/群聊分离

存储逻辑：
├── 私聊消息：chat_history_[userA]_[userB].txt
├── 群聊消息：chat_history_group_[groupId].txt
└── 文件命名：按用户ID字母序保证唯一性
```

#### 4. 会话管理模型

```
在线会话 (OnlineSession)
├── 会话映射：Map<String userId, ClientHandler>
├── 并发安全：ConcurrentHashMap实现
├── 生命周期：登录创建，断连销毁
└── 状态同步：实时更新用户在线状态
```

### 4.2.2 物理结构设计

#### 1. 文件存储结构

```
项目根目录/
├── users.txt              # 用户基本信息
├── friendships.txt        # 好友关系数据
├── friend_requests.txt    # 待处理好友请求
├── groups.txt             # 群组信息和成员
├── group_invites.txt      # 待处理群组邀请
└── chat_history_*.txt     # 聊天记录文件
```

#### 2. 文件格式规范

##### users.txt格式：
```
用户ID|用户名|密码
user001|张三|123456
user002|李四|abcdef
```

##### friendships.txt格式：
```
用户ID|好友ID1|好友ID2|...
user001|user002|user003
user002|user001|user004
```

##### groups.txt格式：
```
群组ID|成员ID1|成员ID2|...
group001|user001|user002|user003
group002|user002|user004
```

##### 聊天记录格式：
```
[时间戳] 发送者ID: 消息内容
[2024-01-01 10:30:00] user001: 你好
[2024-01-01 10:30:15] user002: 你好，很高兴认识你
```

#### 3. 内存数据结构

##### 服务器端内存结构：
```java
// 用户管理
Map<String, User> users;                    // 用户信息缓存
Map<String, List<String>> friendships;      // 好友关系缓存
Map<String, List<String>> pendingRequests;  // 待处理请求

// 群组管理
Map<String, List<String>> groups;           // 群组成员缓存
Map<String, List<String>> groupInvites;     // 群组邀请缓存

// 会话管理
Map<String, ClientHandler> onlineClients;   // 在线用户会话
```

##### 客户端内存结构：
```java
// 界面管理
Map<String, SingleChatWindow> singleChats;  // 私聊窗口缓存
Map<String, GroupChatWindow> groupChats;    // 群聊窗口缓存

// 数据模型
DefaultListModel<String> friendListModel;   // 好友列表模型
DefaultListModel<String> groupListModel;    // 群组列表模型
```

#### 4. 网络传输结构

##### 传输协议：
- **传输层**：TCP Socket保证可靠传输
- **序列化**：Java原生对象序列化
- **消息格式**：Message对象封装所有通信数据
- **编码方式**：UTF-8字符编码

##### 图片传输机制：
```
客户端选择图片 → 文件读取为byte[] → Base64编码 → 
字符串包装在Message中 → 网络传输 → 服务器转发 → 
接收方Base64解码 → 恢复为byte[] → 保存为图片文件
```

## 4.3 系统出错处理设计

### 4.3.1 出错信息分类

#### 1. 网络连接错误

##### 连接建立错误：
- **错误类型**：`ConnectException`、`SocketTimeoutException`
- **触发场景**：服务器未启动、网络不通、端口被占用
- **处理策略**：
  ```java
  try {
      socket = new Socket("127.0.0.1", 8888);
  } catch (ConnectException e) {
      JOptionPane.showMessageDialog(null, 
          "无法连接到服务器，请检查服务器是否启动", 
          "连接错误", JOptionPane.ERROR_MESSAGE);
      return false;
  }
  ```

##### 通信中断错误：
- **错误类型**：`IOException`、`SocketException`
- **触发场景**：网络中断、客户端异常断开
- **处理策略**：自动清理资源，通知其他用户状态变化

#### 2. 用户认证错误

##### 登录失败错误：
- **错误场景**：
  - 用户ID不存在：`MessageType.LOGIN_FAIL` + "User not found"
  - 密码错误：`MessageType.LOGIN_FAIL` + "Invalid password"
  - 重复登录：`MessageType.LOGIN_FAIL` + "User already online"
- **处理方式**：
  ```java
  if (user != null) {
      if (server.isUserOnline(id)) {
          sendMessage(new Message(MessageType.LOGIN_FAIL, 
              "Server", id, "用户已在其他地方登录"));
          return;
      }
      // 登录成功逻辑
  } else {
      sendMessage(new Message(MessageType.LOGIN_FAIL, 
          "Server", id, "用户ID或密码错误"));
  }
  ```

##### 注册失败错误：
- **错误场景**：
  - 用户ID重复：`MessageType.REGISTER_FAIL` + "User ID already exists"
  - 输入格式错误：客户端验证，提示格式要求
- **处理方式**：服务器端验证唯一性，客户端显示具体错误信息

#### 3. 文件操作错误

##### 数据持久化错误：
- **错误类型**：`FileNotFoundException`、`IOException`
- **触发场景**：文件权限不足、磁盘空间不足、文件被占用
- **处理策略**：
  ```java
  @SuppressWarnings("CallToPrintStackTrace")
  private void saveUsers() {
      try (BufferedWriter writer = new BufferedWriter(
              new FileWriter(USERS_FILE))) {
          // 保存逻辑
      } catch (IOException e) {
          System.err.println("保存用户数据失败: " + e.getMessage());
          e.printStackTrace();
          // 不中断服务，记录错误日志
      }
  }
  ```

##### 图片传输错误：
- **错误场景**：文件格式不支持、文件过大、保存路径无效
- **处理方式**：
  ```java
  // 文件大小检查
  if (file.length() > MAX_FILE_SIZE) {
      JOptionPane.showMessageDialog(this, 
          "图片文件过大，请选择小于10MB的文件", 
          "文件错误", JOptionPane.ERROR_MESSAGE);
      return;
  }
  ```

### 4.3.2 错误恢复机制

#### 1. 自动重连机制

##### 客户端重连策略：
```java
private boolean reconnect() {
    int retryCount = 0;
    while (retryCount < MAX_RETRY_COUNT) {
        try {
            Thread.sleep(RETRY_DELAY);
            socket = new Socket("127.0.0.1", 8888);
            setupStreams();
            return true;
        } catch (Exception e) {
            retryCount++;
            System.out.println("重连尝试 " + retryCount + " 失败");
        }
    }
    return false;
}
```

##### 服务器端会话清理：
```java
public synchronized void removeClient(String userId) {
    ClientHandler handler = onlineClients.remove(userId);
    if (handler != null) {
        User user = userManager.getUserById(userId);
        if (user != null) {
            user.setOnline(false);
            // 通知好友状态变化
            notifyFriendsStatusChange(userId);
        }
    }
}
```

#### 2. 数据完整性保护

##### 文件备份策略：
```java
private void saveWithBackup(String filename, String data) {
    String backupFile = filename + ".backup";
    try {
        // 先保存到备份文件
        Files.write(Paths.get(backupFile), data.getBytes());
        // 备份成功后替换原文件
        Files.move(Paths.get(backupFile), Paths.get(filename), 
            StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
        System.err.println("数据保存失败: " + e.getMessage());
    }
}
```

##### 消息队列机制：
```java
// 离线消息缓存
private final Map<String, Queue<Message>> offlineMessages = 
    new ConcurrentHashMap<>();

public void cacheOfflineMessage(String userId, Message message) {
    offlineMessages.computeIfAbsent(userId, 
        k -> new ConcurrentLinkedQueue<>()).offer(message);
}
```

#### 3. 用户友好的错误提示

##### 分级错误提示：
- **致命错误**：红色警告框，建议重启应用
- **一般错误**：黄色提示框，提供解决建议
- **信息提示**：蓝色信息框，说明操作结果

##### 错误信息本地化：
```java
public class ErrorMessages {
    public static final String NETWORK_ERROR = "网络连接异常，请检查网络设置";
    public static final String AUTH_FAILED = "用户名或密码错误，请重新输入";
    public static final String FILE_ERROR = "文件操作失败，请检查文件权限";
    public static final String SYSTEM_BUSY = "系统繁忙，请稍后重试";
}
```

#### 4. 日志记录机制

##### 错误日志格式：
```
[时间戳] [错误级别] [模块名] [错误类型]: 错误描述
[2024-01-01 10:30:00] [ERROR] [UserManager] [FileIOException]: 无法保存用户数据到 users.txt
[2024-01-01 10:30:05] [WARN] [ClientHandler] [SocketException]: 客户端 user001 连接中断
```

##### 日志轮转策略：
- 按日期分割日志文件
- 保留最近30天的日志
- 压缩历史日志文件

---

本文档详细描述了SimpleQQ即时通讯系统的功能模块、数据结构和错误处理机制。系统采用模块化设计，具有良好的可扩展性和可维护性，通过完善的错误处理机制保证系统的稳定性和用户体验。