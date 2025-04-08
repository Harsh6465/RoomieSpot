package com.example.roomiespot.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.TypeConverters;
import androidx.room.Ignore;
import com.example.roomiespot.database.Converters;
import java.util.Date;

@Entity(tableName = "messages")
@TypeConverters(Converters.class)
public class Message {
    @PrimaryKey
    @NonNull
    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private Date timestamp;
    private boolean isRead;
    private String propertyId;

    // Constructor for Room
    public Message() {
        this.isRead = false;
    }

    // Constructor for Firebase
    @Ignore
    public Message(String senderId, String receiverId, String text, String propertyId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.propertyId = propertyId;
        this.timestamp = new Date();
        this.isRead = false;
    }

    // Add getter and setter for propertyId
    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    // Add getter and setter for isRead
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}