package com.example.controllogistica;

import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.barcode.BarcodeScanner;

public class RegistroU extends CatalagoBase {

    private boolean isPaused = false;
    private String chofer;        // Columna A
    private String numCamioneta;  // Columna B
    private String numVueltas;    // Columna C
    private String fecha;         // Columna E
    private String hora;          // Columna F

    // Constructor para crear un registro nuevo rápidamente
    public RegistroU(String chofer, String numCamioneta, String numVueltas, String fecha, String hora) {
        this.chofer = chofer;
        this.numCamioneta = numCamioneta;
        this.numVueltas = numVueltas;
        this.fecha = fecha;
        this.hora = hora;
    }


    public String getChofer() { return chofer; }
    public String getNumCamioneta() { return numCamioneta; }
    public String getNumVueltas() { return numVueltas; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
}