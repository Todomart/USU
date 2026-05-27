package com.example.controllogistica;



/**
 * Representa una fila completa en la hoja de cálculo.
 * Mapea los datos de la Columna A hasta la G.
 */
public class RegistroU extends CatalagoBase {

    private String chofer;        // Columna A
    private String numCamioneta;  // Columna B
    private String numVueltas;    // Columna C
    private String ruta;          // Columna D
    private String estatus;       // Columna E (Vueltas)
    private String fecha;         // Columna F
    private String hora;          // Columna G

    // Constructor para crear un registro nuevo rápidamente
    public RegistroU(String chofer, String numCamioneta, String numVueltas, String ruta, String estatus, String fecha, String hora) {
        this.chofer = chofer;
        this.numCamioneta = numCamioneta;
        this.numVueltas = numVueltas;
        this.ruta = ruta;
        this.estatus = estatus;
        this.fecha = fecha;
        this.hora = hora;
    }

    // Getters (Para que el MainActivity pueda leer los datos y enviarlos por Volley)
    public String getChofer() { return chofer; }
    public String getNumCamioneta() { return numCamioneta; }
    public String getNumVueltas() { return numVueltas; }
    public String getRuta() { return ruta; }
    public String getEstatus() { return estatus; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
}