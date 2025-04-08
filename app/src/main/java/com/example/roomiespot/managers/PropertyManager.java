package com.example.roomiespot.managers;

import android.content.Context;

public class PropertyManager {
    private static PropertyManager instance;
    private Context context;

    private PropertyManager() {}

    public static PropertyManager getInstance() {
        if (instance == null) {
            instance = new PropertyManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context.getApplicationContext();
    }

    // ... rest of the existing methods ...
}