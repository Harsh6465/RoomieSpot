package com.example.roomiespot.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.Ignore;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    private String userId;
    private String fullName;
    private String email;
    private String profileImageUrl;

    // Constructor for Room
    public User() {}

    // Constructor for Firebase
    @Ignore
    public User(String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}