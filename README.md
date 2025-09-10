# SimpleQQ 即时通讯系统

## 项目概述

SimpleQQ是一个基于Java技术栈开发的即时通讯应用程序，采用经典的客户端-服务器(C/S)架构设计。系统集成了用户管理、好友系统、私聊群聊、文件传输等核心功能，为用户提供完整的即时通讯解决方案。

### 核心特性
- **多用户支持**：支持多用户同时在线，实时消息传输
- **好友系统**：完整的好友添加、删除、状态管理功能
- **聊天功能**：支持私聊、群聊两种聊天模式
- **文件传输**：支持图片文件的发送和接收
- **数据持久化**：聊天记录自动保存，支持历史记录查看
- **状态同步**：实时显示用户在线/离线状态

### 技术架构
- **后端技术**：Java 17, Socket网络编程, 多线程并发处理
- **前端技术**：Java Swing, 事件驱动编程
- **数据存储**：文件系统持久化存储
- **通信协议**：基于TCP的自定义消息协议
- **项目管理**：Maven多模块项目结构

## 项目结构

### 模块组织
```
SimpleQQ/
├── pom.xml                     # Maven父项目配置
├── common/                     # 公共模块 - 共享数据结构和协议
│   ├── pom.xml
│   └── src/main/java/com/simpleqq/common/
│       ├── Message.java        # 消息实体类，定义通信格式
│       ├── MessageType.java    # 消息类型枚举，规范协议类型
│       └── User.java           # 用户实体类，封装用户信息
├── server/                     # 服务器端模块 - 处理业务逻辑和数据管理
│   ├── pom.xml
│   └── src/main/java/com/simpleqq/server/
│       ├── Server.java         # 服务器主类，管理连接和消息分发
│       ├── ClientHandler.java  # 客户端处理器，独立线程处理请求
│       ├── UserManager.java    # 用户管理器，处理用户和好友关系
│       └── GroupManager.java   # 群组管理器，处理群组相关功能
└── client/                     # 客户端模块 - 用户界面和交互逻辑
    ├── pom.xml
    └── src/main/java/com/simpleqq/client/
        ├── LoginWindow.java     # 登录窗口，用户认证界面
        ├── RegisterWindow.java  # 注册窗口，新用户注册
        ├── ChatWindow.java      # 主聊天窗口，好友和群组管理
        ├── SingleChatWindow.java # 私聊窗口，一对一聊天
        ├── GroupChatWindow.java  # 群聊窗口，群组聊天
        └── Client.java          # 客户端核心类，网络通信处理
```

### 数据文件结构
```
运行目录/
├── users.txt                   # 用户基本信息存储
├── friendships.txt             # 好友关系数据
├── friend_requests.txt         # 待处理好友请求
├── groups.txt                  # 群组信息和成员列表
├── group_invites.txt           # 待处理群组邀请
├── .history/                   # 聊天记录目录
│   ├── chat_history_user1_user2.txt    # 私聊记录
│   └── chat_history_group_groupId.txt  # 群聊记录
└── received_images_from_[用户ID]/       # 接收的图片文件
```

## 功能详述

### 用户管理系统

#### 用户注册
- **功能**：支持新用户账户创建
- **验证机制**：
  - 用户ID唯一性检查
  - 输入格式验证（非空、长度限制）
  - 密码强度要求
- **数据存储**：用户信息实时写入`users.txt`文件
- **错误处理**：重复ID提示、格式错误反馈

#### 用户登录
- **认证流程**：
  1. 客户端发送LOGIN消息包含用户ID和密码
  2. 服务器验证用户凭据的有效性
  3. 检查用户是否已在其他地方登录
  4. 验证成功后建立会话并更新在线状态
- **安全特性**：
  - 防止重复登录（一个账户同时只能在一处登录）
  - 密码明文存储（注：实际项目中应使用加密存储）
  - 会话管理和超时处理

### 好友系统

#### 好友请求机制
- **发送请求**：
  - 输入好友ID发送添加请求
  - 系统验证目标用户存在性
  - 检查是否已为好友或已有待处理请求
  - 请求存储在`friend_requests.txt`中
- **处理请求**：
  - 接收方可以接受或拒绝请求
  - 接受后双方建立好友关系
  - 拒绝后从待处理列表中移除
  - 实时通知请求处理结果

#### 好友关系管理
- **关系存储**：双向好友关系存储在`friendships.txt`
- **状态显示**：实时显示好友在线/离线状态
- **好友删除**：支持单方面删除好友关系
- **列表刷新**：好友状态变化时自动更新界面

### 聊天功能

#### 私聊功能
- **窗口管理**：
  - 点击好友头像打开独立私聊窗口
  - 每个好友对应一个`SingleChatWindow`实例
  - 窗口复用，避免重复创建
- **消息处理**：
  - 实时消息传输和显示
  - 消息时间戳显示
  - 发送状态反馈
- **聊天记录**：
  - 自动保存到`chat_history_用户A_用户B.txt`
  - 按时间顺序记录所有消息
  - 支持聊天记录导出功能

#### 群聊功能
- **群组管理**：
  - 任何用户都可以创建群组
  - 创建者自动成为群组成员
  - 群组ID必须唯一
- **成员管理**：
  - 支持邀请其他用户加入群组
  - 被邀请者可以接受或拒绝邀请
  - 实时显示群组成员列表和在线状态
- **消息广播**：
  - 群组消息自动转发给所有在线成员
  - 消息发送者验证（只有群成员可以发送消息）
  - 群聊记录保存到`chat_history_group_群组ID.txt`

### 文件传输系统

#### 图片传输机制
- **发送流程**：
  1. 用户选择图片文件（支持常见格式：jpg, png, gif等）
  2. 客户端读取文件并转换为Base64编码
  3. 封装为IMAGE_MESSAGE类型发送到服务器
  4. 服务器转发给目标用户或群组成员
- **接收处理**：
  1. 接收方自动解码Base64数据
  2. 恢复为原始图片文件
  3. 保存到`received_images_from_[发送者ID]/`目录
  4. 在聊天窗口显示图片接收提示
- **存储优化**：
  - 聊天记录只保存图片文件名，不保存Base64数据
  - 自动创建发送者专用的图片存储目录
  - 支持大文件传输（建议限制在10MB以内）

## 系统架构设计

### 网络通信架构

#### 服务器端设计
```
ServerSocket (端口8888)
      ↓
接受客户端连接请求
      ↓
为每个连接创建 ClientHandler 线程
      ↓
┌─────────────────────────────────┐
│        ClientHandler            │
│  ┌─────────────────────────┐    │
│  │   ObjectInputStream     │    │
│  │  (接收客户端消息)         │    │
│  └─────────────────────────┘    │
│             ↓                   │
│  ┌─────────────────────────┐    │
│  │    消息类型分发处理        │    │
│  │ LOGIN/REGISTER/FRIEND/    │    │
│  │ TEXT_MESSAGE/GROUP等      │    │
│  └─────────────────────────┘    │
│             ↓                   │
│  ┌─────────────────────────┐    │
│  │   ObjectOutputStream    │    │
│  │  (发送响应消息)          │    │
│  └─────────────────────────┘    │
└─────────────────────────────────┘
```

#### 客户端设计
```
┌─────────────────────────────────┐
│           Client               │
│  ┌─────────────────────────┐    │
│  │      Socket连接         │    │
│  │   (连接到服务器:8888)     │    │
│  └─────────────────────────┘    │
│             ↓                   │
│  ┌─────────────────────────┐    │
│  │    消息监听线程          │    │
│  │  (持续接收服务器消息)     │    │
│  └─────────────────────────┘    │
│             ↓                   │
│  ┌─────────────────────────┐    │
│  │      UI事件处理         │    │
│  │  (Swing界面交互逻辑)     │    │
│  └─────────────────────────┘    │
└─────────────────────────────────┘
```

### 消息协议设计

#### 消息格式
```java
public class Message implements Serializable {
    private MessageType type;      // 消息类型（必填）
    private String senderId;       // 发送者用户ID（必填）
    private String receiverId;     // 接收者ID或群组ID（必填）
    private long timestamp;        // 消息时间戳（自动生成）
    private String content;        // 消息内容（可选）
}
```

#### 消息类型分类
```java
// 用户认证类消息
LOGIN, REGISTER, LOGIN_SUCCESS, LOGIN_FAIL, REGISTER_SUCCESS, REGISTER_FAIL

// 好友管理类消息
FRIEND_REQUEST, FRIEND_ACCEPT, FRIEND_REJECT, DELETE_FRIEND, 
ADD_FRIEND_SUCCESS, ADD_FRIEND_FAIL, DELETE_FRIEND_SUCCESS, DELETE_FRIEND_FAIL

// 聊天消息类
TEXT_MESSAGE, IMAGE_MESSAGE, GROUP_MESSAGE

// 群组管理类消息
CREATE_GROUP, GROUP_INVITE, GROUP_ACCEPT, GROUP_REJECT,
CREATE_GROUP_SUCCESS, CREATE_GROUP_FAIL, GROUP_JOIN_SUCCESS, GROUP_JOIN_FAIL

// 数据获取类消息
FRIEND_LIST, GET_GROUPS, GET_GROUP_MEMBERS, GET_PENDING_REQUESTS

// 系统消息类
SERVER_MESSAGE
```

### 数据持久化设计

#### 文件存储格式

##### 用户信息存储（users.txt）
```
格式：用户ID|用户名|密码
示例：
user001|张三|123456
user002|李四|password123
admin|管理员|admin888
```

##### 好友关系存储（friendships.txt）
```
格式：用户ID|好友ID1|好友ID2|...
示例：
user001|user002|user003|user005
user002|user001|user004
user003|user001|user004|user005
```

##### 群组信息存储（groups.txt）
```
格式：群组ID|成员ID1|成员ID2|...
示例：
tech_group|user001|user002|user003
family_chat|user001|user004|user005
study_group|user002|user003|user004
```

##### 聊天记录存储
```
格式：[时间戳] [发送者] to [接收者]: 消息内容
示例：
2024-01-15 14:30:25 [user001] to [user002]: 你好，今天天气不错
2024-01-15 14:30:40 [user002] to [user001]: 是啊，适合出去走走
2024-01-15 14:31:05 [user001] to [user002]: [图片: photo_20240115_143105.jpg]
```

## 环境要求与部署

### 系统要求
- **Java版本**：JDK 17 或更高版本
- **构建工具**：Apache Maven 3.6+
- **操作系统**：Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **内存要求**：最小512MB，推荐1GB+
- **磁盘空间**：至少100MB可用空间（用于聊天记录和图片存储）
- **网络要求**：服务器需要开放8888端口

### 编译与构建

#### 获取源代码
```bash
# 克隆项目仓库
git clone https://github.com/scnscnscn/OICQ.git
cd OICQ/QQ

# 检查Java版本
java -version
# 应该显示Java 17或更高版本

# 检查Maven版本
mvn -version
# 应该显示Maven 3.6或更高版本
```

#### 编译项目
```bash
# 清理并编译所有模块
mvn clean compile

# 打包项目（生成JAR文件）
mvn clean package

# 只编译不运行测试
mvn clean compile -DskipTests

# 查看编译结果
ls -la */target/classes/
```

#### 项目结构验证
```bash
# 验证编译后的文件结构
find . -name "*.class" | head -10
# 应该显示编译好的class文件

# 检查依赖关系
mvn dependency:tree
```

### 部署与运行

#### 服务器端部署
```bash
# 方式1：使用Maven直接运行
cd server
mvn exec:java -Dexec.mainClass="com.simpleqq.server.Server"

# 方式2：使用编译后的class文件运行
cd server/target/classes
java -cp ../../common/target/classes:. com.simpleqq.server.Server

# 方式3：使用JAR文件运行（需要先打包）
cd server/target
java -cp ../../../common/target/common-1.0-SNAPSHOT.jar:server-1.0-SNAPSHOT.jar com.simpleqq.server.Server
```

#### 客户端部署
```bash
# 方式1：使用Maven直接运行
cd client
mvn exec:java -Dexec.mainClass="com.simpleqq.client.LoginWindow"

# 方式2：使用编译后的class文件运行
cd client/target/classes
java -cp ../../common/target/classes:. com.simpleqq.client.LoginWindow

# 方式3：后台运行客户端
nohup java -cp ../../common/target/classes:. com.simpleqq.client.LoginWindow &
```

#### 服务器配置
```bash
# 检查端口占用情况
netstat -an | grep 8888
# 如果端口被占用，需要先停止占用进程

# 防火墙配置（Linux）
sudo ufw allow 8888
sudo firewall-cmd --add-port=8888/tcp --permanent

# 防火墙配置（Windows）
# 需要在Windows防火墙中添加8888端口的入站规则
```

## 使用指南

### 用户操作流程

#### 新用户注册
1. **启动客户端**：运行LoginWindow程序
2. **选择注册**：点击"注册"按钮打开注册界面
3. **填写信息**：
   - 用户ID：输入唯一的用户标识（如：user001）
   - 用户名：输入显示名称（如：张三）
   - 密码：输入登录密码
4. **提交注册**：点击"注册"按钮完成账户创建
5. **返回登录**：注册成功后自动返回登录界面

#### 用户登录
1. **输入凭据**：在登录界面输入用户ID和密码
2. **点击登录**：系统验证用户身份
3. **进入主界面**：验证成功后打开主聊天窗口

#### 好友管理操作
1. **添加好友**：
   - 点击"添加好友"按钮
   - 输入要添加的好友ID
   - 系统发送好友请求给对方
2. **处理好友请求**：
   - 切换到"请求"标签页
   - 选择待处理的好友请求
   - 点击"接受好友"或"拒绝好友"
3. **删除好友**：
   - 在好友列表中选择要删除的好友
   - 点击"删除好友"按钮
   - 确认删除操作

#### 聊天操作
1. **私聊**：
   - 在好友列表中双击好友头像
   - 在弹出的聊天窗口中输入消息
   - 按Enter键或点击"发送"按钮
2. **群聊**：
   - 首先创建群组或接受群组邀请
   - 双击群组名称打开群聊窗口
   - 发送消息给群组所有成员
3. **发送图片**：
   - 在聊天窗口中点击"发送图片"按钮
   - 选择要发送的图片文件
   - 图片会自动发送给对方

#### 群组管理操作
1. **创建群组**：
   - 点击"创建群聊"按钮
   - 输入群组ID（必须唯一）
   - 群组创建成功后显示在群组列表中
2. **邀请成员**：
   - 在群聊窗口中点击"邀请成员"
   - 输入要邀请的用户ID
   - 对方会收到群组邀请通知
3. **管理群组**：
   - 查看群组成员列表和在线状态
   - 处理群组相关消息

## 故障排除

### 常见问题及解决方案

#### 连接问题
**问题**：客户端无法连接到服务器
**解决方案**：
1. 确认服务器已启动：`netstat -an | grep 8888`
2. 检查防火墙设置：确保8888端口未被阻挡
3. 验证网络连通性：`telnet 127.0.0.1 8888`
4. 检查Java版本兼容性
5. 查看服务器日志输出

#### 认证问题
**问题**：登录失败
**解决方案**：
1. 确认用户ID和密码正确
2. 检查用户是否已在其他地方登录
3. 验证`users.txt`文件格式正确
4. 确认文件读写权限正常

#### 功能问题
**问题**：好友请求发送失败
**解决方案**：
1. 确认目标用户ID存在
2. 检查是否已经是好友关系
3. 验证是否有重复的待处理请求
4. 确认`friend_requests.txt`文件正常

**问题**：图片发送失败
**解决方案**：
1. 检查图片文件大小（建议<10MB）
2. 确认文件格式支持
3. 验证文件读取权限
4. 检查网络传输稳定性
5. 确认接收方在线状态

## 扩展开发

### 功能扩展建议

#### 即将实现的功能
1. **消息加密**：添加端到端加密保护
2. **文件传输**：支持任意类型文件传输
3. **语音通话**：集成语音通话功能
4. **视频聊天**：添加视频通话支持
5. **消息撤回**：支持已发送消息撤回

#### 性能优化方向
1. **数据库集成**：使用MySQL/PostgreSQL替代文件存储
2. **缓存机制**：添加Redis缓存提高性能
3. **负载均衡**：支持多服务器部署
4. **消息队列**：使用RabbitMQ处理消息
5. **微服务架构**：拆分为多个独立服务

#### 用户体验改进
1. **响应式UI**：改进界面适配不同屏幕
2. **主题支持**：添加多种界面主题
3. **快捷键**：增加键盘快捷操作
4. **表情包**：支持表情符号和表情包
5. **消息格式**：支持富文本和Markdown

### 开发环境配置

#### 代码规范
1. **命名规范**：使用有意义的变量和方法名
2. **注释规范**：重要方法添加详细注释
3. **异常处理**：合理捕获和处理异常
4. **线程安全**：注意并发访问的数据同步
5. **资源管理**：及时关闭网络连接和文件流

#### 测试建议
1. **单元测试**：为关键方法编写单元测试
2. **集成测试**：测试客户端-服务器交互
3. **压力测试**：测试多用户并发场景
4. **异常测试**：测试各种错误处理情况

## 许可证与贡献

### 开源许可证
本项目采用 Apache License 2.0 许可证，详见 [LICENSE](LICENSE) 文件。

### 贡献指南
欢迎提交 Issue 和 Pull Request 来改进项目：

1. **提交Bug报告**：详细描述问题和重现步骤
2. **功能建议**：说明新功能的需求和设计思路
3. **代码贡献**：遵循项目代码规范和提交规范
4. **文档改进**：完善用户手册和开发文档

### 联系方式
- **项目仓库**：https://github.com/scnscnscn/OICQ
- **问题反馈**：通过GitHub Issues提交
- **技术交流**：欢迎在Issues中进行技术讨论

---

*最后更新：2024年1月*
*文档版本：v2.0*