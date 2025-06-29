import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    // 日志记录器
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    // 监听端口，可通过命令行参数指定
    private static final int PORT = getPortFromArgs();
    // 保存客户端连接，key为用户ID，value为对应的客户端处理线程
    private static Map<String, ClientHandler> clientMap = new HashMap<>();
    // 用户管理对象
    private static UserManager userManager = new UserManager();
    // 群组管理对象
    private static GroupManager groupManager = new GroupManager();
    // 线程池
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器启动，监听端口: " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, userManager, groupManager, clientMap);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "服务器启动失败或监听出错", e);
        } finally {
            threadPool.shutdown();
        }
    }

    // 从命令行参数获取端口号，若未指定则使用默认值
    private static int getPortFromArgs() {
        if (System.getProperties().containsKey("port")) {
            try {
                return Integer.parseInt(System.getProperty("port"));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "端口号格式错误，使用默认端口", e);
            }
        }
        return 8888;
    }

    // 提供获取客户端连接映射的方法，供ClientHandler等使用
    public static Map<String, ClientHandler> getClientMap() {
        return clientMap;
    }
}