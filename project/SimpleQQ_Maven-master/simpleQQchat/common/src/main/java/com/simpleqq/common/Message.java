package com.simpleqq.common;

import java.io.Serializable;

/**
 * 消息类，用于封装聊天消息的数据
 */
public class Message implements Serializable {
    // 消息类型
    private MessageType type;
    // 发送者ID
    private String senderId;
    // 接收者ID
    private String receiverId;
    // 消息发送的时间戳
    private long timestamp;
    // 消息内容
    private String content;

    public Message(MessageType type, String senderId, String receiverId, String content) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = System.currentTimeMillis();
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }
    public String getSenderId() {
        return senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public String getContent() {
        return content;
    }

    // 设置消息类型
    public void setType(MessageType type) {
        this.type = type;
    }

    // 设置发送者ID
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    // 设置接收者ID
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // 设置时间戳
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // 设置消息内容
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
               "type=" + type +
               ", senderId='" + senderId + '\'' +
               ", receiverId='" + receiverId + '\'' +
               ", timestamp=" + timestamp +
               ", content='" + content + '\'' +
               '}';
    }
}
