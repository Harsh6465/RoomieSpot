package com.example.roomiespot.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.roomiespot.models.User;
import com.example.roomiespot.models.Property;
import com.example.roomiespot.models.Message;

@Database(entities = {User.class, Property.class, Message.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    private static AppDatabase instance;
    
    // Define DAOs here
    // public abstract UserDao userDao();
    // public abstract PropertyDao propertyDao();
    // public abstract MessageDao messageDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "roomiespot_database")
                .fallbackToDestructiveMigration()
                .build();
        }
        return instance;
    }
}