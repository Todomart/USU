package com.example.controllogistica;
import android.app.Application;
import androidx.room.Room;
import com.example.controllogistica.data.AppDatabase;

public class MyApplication extends Application {
    public static AppDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "logistica-db").build();
    }
}