package com.example.controllogistica.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao // Esto es lo que faltaba o estaba mal
public interface RegistroDao {

    @Insert // Esto le dice a Room que el método es para guardar
    void insert(Registro registro);

    @Query("SELECT * FROM registro WHERE sincronizado = 0") // Esto es para obtener los pendientes[cite: 1]
    List<Registro> getRegistrosPendientes();

    @Query("UPDATE registro SET sincronizado = 1 WHERE id = :id") // Esto es para marcar como enviado[cite: 1]
    void marcarSincronizado(int id);

    @Update
    void update(Registro registro);
    @Delete
    void delete(Registro registro);
    @Query("SELECT * FROM Registro WHERE sincronizado = 0")
    List<Registro> getNoSincronizados();
}