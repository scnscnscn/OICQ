import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private Socket socket;
    private UserManager userManager;
    private GroupManager groupManager;
    private Map<String, ClientHandler> clientMap;
    // 当前客户端对应的用户ID，登录后赋值
    private String userId;
    private OutputStreamWriter osw;

    public ClientHandler(Socket socket, UserManager userManager, GroupManager groupManager, Map<String, ClientHandler> clientMap) {
        this.socket = socket;
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.clientMap = clientMap;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            osw = new OutputStreamWriter(socket.getOutputStream());
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 1) {
                    osw.write("无效的协议格式！\n");
                    osw.flush();
                    continue;
                }
                // 简单协议，第一个字段表示操作类型，如LOGIN、CHAT、ADD_FRIEND等
                String operation = parts[0];
                switch (operation) {
                    case "LOGIN":
                        if (parts.length < 2) {
                            osw.write("登录协议格式错误！\n");
                            osw.flush();
                            continue;
                        }
                        // 协议格式：LOGIN|userId
                        userId = parts[1];
                        clientMap.put(userId, this);
                        userManager.addUser(userId);
                        osw.write("登录成功！\n");
                        osw.flush();
                        break;
                    // ... 其他 case 也可以添加类似的输入验证 ...
                    default:
                        osw.write("不支持的操作！\n");
                        osw.flush();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "客户端连接出错", e);
            // 客户端断开连接，从clientMap中移除
            if (userId != null) {
                clientMap.remove(userId);
            }
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "关闭输出流出错", e);
                }
            }
        }
    }

    // 给当前客户端发送消息的方法
    public void sendMessage(String message) {
        if (osw != null) {
            try {
                osw.write(message + "\n");
                osw.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "发送消息出错", e);
            }
        }
    }
}