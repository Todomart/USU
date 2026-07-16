package com.example.controllogistica.data;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "registro")
public class Registro{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String Chofer, numCamioneta, fecha, hora ;
    public int sincronizado;
}
