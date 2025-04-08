package com.example.roomiespot.models;

import com.google.firebase.Timestamp;

public class Review {
    private String id;
    private String propertyId;
    private String userId;
    private String userName;
    private String userPhotoUrl;
    private float rating;
    private String comment;
    private Timestamp createdAt;

    // Empty constructor needed for Firestore
    public Review() {
    }

    public Review(String id, String propertyId, String userId, String userName, String userPhotoUrl,
                 float rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.userId = userId;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}