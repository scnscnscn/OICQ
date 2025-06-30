package com.simpleqq.common;

import java.io.Serializable;

/**
 * 用户类，表示一个用户的信息
 */
public class User implements Serializable {
    // 用户ID
    private String id;
    // 用户名
    private String username;
    // 用户密码
    private String password;
    // 用户是否在线
    private boolean isOnline;

    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isOnline = false;
    }

    public String getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }

    /**
     * 判断用户是否在线
     * @return 是否在线
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * 设置用户在线状态
     * @param online 是否在线
     */
    public void setOnline(boolean online) {
        isOnline = online;
    }

    /**
     * 返回用户信息的字符串表示
     * @return 用户信息字符串
     */
    @Override
    public String toString() {
        return "User{" +
               "id=\'" + id + '\'' +
               ", username=\'" + username + '\'' +
               ", password=\'" + password + '\'' +
               ", isOnline=" + isOnline +
               '}';
    }
}
