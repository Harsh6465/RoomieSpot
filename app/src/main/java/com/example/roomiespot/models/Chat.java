package com.example.roomiespot.models;

public class Chat {
    private String userId;
    private String propertyId;
    private long lastMessageTime;
    private boolean hasUnreadMessages;
    private String lastMessage;
    private String userName;
    private String propertyTitle;

    // Empty constructor needed for Firestore
    public Chat() {
    }

    public Chat(String userId, String propertyId, long lastMessageTime) {
        this.userId = userId;
        this.propertyId = propertyId;
        this.lastMessageTime = lastMessageTime;
        this.hasUnreadMessages = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isHasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }
}