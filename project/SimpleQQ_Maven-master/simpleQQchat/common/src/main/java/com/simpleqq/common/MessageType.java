package com.simpleqq.common;

/**
 * 消息类型枚举
 * 用于定义客户端与服务器之间通信的各种消息类型
 */
public enum MessageType {
    LOGIN,                  // 登录
    REGISTER,               // 注册
    ADD_FRIEND,             // 添加好友
    DELETE_FRIEND,          // 删除好友
    TEXT_MESSAGE,           // 文本消息
    IMAGE_MESSAGE,          // 图片消息
    GROUP_MESSAGE,          // 群消息
    ONLINE_USERS,           // 在线用户列表
    FRIEND_LIST,            // 好友列表
    LOGIN_SUCCESS,          // 登录成功
    LOGIN_FAIL,             // 登录失败
    REGISTER_SUCCESS,       // 注册成功
    REGISTER_FAIL,          // 注册失败
    ADD_FRIEND_SUCCESS,     // 添加好友成功
    ADD_FRIEND_FAIL,        // 添加好友失败
    DELETE_FRIEND_SUCCESS,  // 删除好友成功
    DELETE_FRIEND_FAIL,     // 删除好友失败
    SERVER_MESSAGE,         // 服务器消息
    FRIEND_REQUEST,         // 好友请求
    FRIEND_ACCEPT,          // 同意好友请求
    FRIEND_REJECT,          // 拒绝好友请求
    GROUP_INVITE,           // 群邀请
    GROUP_ACCEPT,           // 同意群邀请
    GROUP_REJECT,           // 拒绝群邀请
    CREATE_GROUP,           // 创建群
    CREATE_GROUP_SUCCESS,   // 创建群成功
    CREATE_GROUP_FAIL,      // 创建群失败
    GET_GROUPS,             // 获取群列表
    GET_PENDING_REQUESTS,   // 获取待处理请求
    GET_GROUP_MEMBERS,      // 获取群成员
    IMAGE_REQUEST,          // 图片发送请求
    IMAGE_ACCEPT,           // 同意接收图片
    IMAGE_REJECT,           // 拒绝接收图片
    IMAGE_DATA,             // 图片数据
    GROUP_JOIN_SUCCESS,     // 加群成功
    GROUP_JOIN_FAIL         // 加群失败
}
