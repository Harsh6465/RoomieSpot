package com.example.roomiespot.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;
import com.example.roomiespot.database.Converters;

@Entity(tableName = "properties")
@TypeConverters(Converters.class)
public class Property {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private double price;
    private String location;
    private List<String> imageUrls;
    private String ownerId;
    private String description;
    private int bedrooms;
    private int bathrooms;
    private double squareFeet;
    private boolean petsAllowed;
    private boolean furnished;
    private List<String> amenities;

    // Default constructor for Firebase
    public Property() {}

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFullAddress() {
        return location != null ? location : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public double getSquareFeet() {
        return squareFeet;
    }

    public void setSquareFeet(double squareFeet) {
        this.squareFeet = squareFeet;
    }

    public boolean isPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }

    public boolean isFurnished() {
        return furnished;
    }

    public void setFurnished(boolean furnished) {
        this.furnished = furnished;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
}