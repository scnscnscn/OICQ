import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 群组管理类，负责群组的创建、用户添加、用户移除以及获取群组用户等操作。
 */
public class GroupManager {
    // key为群组ID，value为群组包含的用户ID集合
    private final Map<String, Set<String>> groupMap = new HashMap<>();

    /**
     * 创建群组
     *
     * @param groupId 群组ID，不能为 null 或空字符串
     * @return 如果群组是新创建的返回 true，若群组已存在则返回 false
     */
    public boolean createGroup(String groupId) {
        // 检查群组 ID 是否为 null 或空字符串
        if (groupId == null || groupId.isEmpty()) {
            return false;
        }
        return groupMap.putIfAbsent(groupId, new HashSet<>()) == null;
    }

    /**
     * 添加用户到群组
     *
     * @param groupId 群组ID，不能为 null 或空字符串
     * @param userId  用户ID，不能为 null 或空字符串
     * @return 如果用户成功添加到群组返回 true，若群组不存在则返回 false
     */
    public boolean addUserToGroup(String groupId, String userId) {
        // 检查群组 ID 和用户 ID 是否为 null 或空字符串
        if (groupId == null || groupId.isEmpty() || userId == null || userId.isEmpty()) {
            return false;
        }
        Set<String> userSet = groupMap.get(groupId);
        if (userSet != null) {
            userSet.add(userId);
            return true;
        }
        return false;
    }

    /**
     * 从群组移除用户
     *
     * @param groupId 群组ID，不能为 null 或空字符串
     * @param userId  用户ID，不能为 null 或空字符串
     * @return 如果用户成功从群组移除返回 true，若群组不存在或用户不在群组中则返回 false
     */
    public boolean removeUserFromGroup(String groupId, String userId) {
        // 检查群组 ID 和用户 ID 是否为 null 或空字符串
        if (groupId == null || groupId.isEmpty() || userId == null || userId.isEmpty()) {
            return false;
        }
        Set<String> userSet = groupMap.get(groupId);
        if (userSet != null && userSet.contains(userId)) {
            userSet.remove(userId);
            return true;
        }
        return false;
    }

    /**
     * 获取群组内的所有用户
     *
     * @param groupId 群组ID，不能为 null 或空字符串
     * @return 群组内的用户ID集合，若群组不存在则返回空集合
     */
    public Set<String> getGroupUsers(String groupId) {
        // 检查群组 ID 是否为 null 或空字符串
        if (groupId == null || groupId.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(groupMap.getOrDefault(groupId, new HashSet<>()));
    }
}