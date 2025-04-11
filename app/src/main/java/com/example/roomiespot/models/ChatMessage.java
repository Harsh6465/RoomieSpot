package com.example.roomiespot.models;

import java.util.Date;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
    private MessageType messageType;

    // Enum for message types
    public enum MessageType {
        TEXT,
        IMAGE,
        LOCATION
    }

    // Default constructor (required for Firebase)
    public ChatMessage() {
    }

    // Parameterized constructor
    public ChatMessage(String senderId, String receiverId, String content, MessageType messageType) {
        this.messageId = generateMessageId();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.messageType = messageType;
    }

    // Generates a unique message ID
    private String generateMessageId() {
        return "MSG_" + System.currentTimeMillis();
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", messageType=" + messageType +
                '}';
    }
}