import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理类，负责管理用户信息和好友关系。
 */
public class UserManager {
    // 保存所有用户，key 为用户 ID
    private Map<String, User> userMap = new HashMap<>();

    /**
     * 添加用户
     *
     * @param userId 用户 ID，不能为 null 或空字符串
     */
    public void addUser(String userId) {
        // 检查用户 ID 是否为 null 或空字符串
        if (userId == null || userId.isEmpty()) {
            return;
        }
        userMap.putIfAbsent(userId, new User(userId));
    }

    /**
     * 添加好友关系
     *
     * @param userId       用户 ID，不能为 null 或空字符串
     * @param friendUserId 好友用户 ID，不能为 null 或空字符串
     * @return 如果成功添加好友关系返回 true，否则返回 false
     */
    public boolean addFriend(String userId, String friendUserId) {
        // 检查用户 ID 和好友用户 ID 是否为 null 或空字符串，以及是否为同一用户
        if (userId == null || userId.isEmpty() || friendUserId == null || friendUserId.isEmpty() || userId.equals(friendUserId)) {
            return false;
        }
        User user = userMap.get(userId);
        User friend = userMap.get(friendUserId);
        // 检查用户和好友对象是否存在，且用户的好友列表中不包含该好友
        if (user != null && friend != null && !user.getFriends().contains(friendUserId)) {
            user.getFriends().add(friendUserId);
            friend.getFriends().add(userId);
            return true;
        }
        return false;
    }

    /**
     * 删除好友关系
     *
     * @param userId       用户 ID，不能为 null 或空字符串
     * @param friendUserId 好友用户 ID，不能为 null 或空字符串
     * @return 如果成功删除好友关系返回 true，否则返回 false
     */
    public boolean deleteFriend(String userId, String friendUserId) {
        // 检查用户 ID 和好友用户 ID 是否为 null 或空字符串
        if (userId == null || userId.isEmpty() || friendUserId == null || friendUserId.isEmpty()) {
            return false;
        }
        User user = userMap.get(userId);
        User friend = userMap.get(friendUserId);
        // 检查用户和好友对象是否存在，且用户的好友列表中包含该好友
        if (user != null && friend != null && user.getFriends().contains(friendUserId)) {
            user.getFriends().remove(friendUserId);
            friend.getFriends().remove(userId);
            return true;
        }
        return false;
    }

    // 内部用户类，可扩展更多属性，如昵称、密码等
    private static class User {
        private String userId;
        private Set<String> friends = new HashSet<>();

        public User(String userId) {
            this.userId = userId;
        }

        public Set<String> getFriends() {
            return friends;
        }
    }
}