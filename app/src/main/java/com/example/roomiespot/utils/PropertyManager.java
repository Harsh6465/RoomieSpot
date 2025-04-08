package com.example.roomiespot.utils;

import com.example.roomiespot.models.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage properties in the app
 */
public class PropertyManager {
    private static PropertyManager instance;
    private List<Property> properties;

    private PropertyManager() {
        properties = new ArrayList<>();
    }

    public static synchronized PropertyManager getInstance() {
        if (instance == null) {
            instance = new PropertyManager();
        }
        return instance;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public void removeProperty(Property property) {
        properties.remove(property);
    }

    public void clearProperties() {
        properties.clear();
    }
}