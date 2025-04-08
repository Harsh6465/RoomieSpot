package com.example.roomiespot.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "images")
public class ImageEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String propertyId;
    private String localPath;
    private long timestamp;
    private boolean isCompressed;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isCompressed() { return isCompressed; }
    public void setCompressed(boolean compressed) { isCompressed = compressed; }
}