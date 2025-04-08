package com.example.roomiespot;

import android.app.Application;
import com.example.roomiespot.database.AppDatabase;
import com.google.firebase.FirebaseApp;

public class RoomieSpotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        AppDatabase.getInstance(this);
    }
}