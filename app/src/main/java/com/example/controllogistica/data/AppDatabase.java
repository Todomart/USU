package com.example.controllogistica.data;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Registro.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RegistroDao registroDao();
}