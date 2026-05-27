package com.example.controllogistica;

/**
 * Clase que representa a un usuario (Chofer o Administrador).
 * Al extender de CatalagoBase, tiene acceso a los nombres de las columnas del Excel.
 */
public class CatalogoChofer extends CatalagoBase {

    // 1. Atributos privados (Encapsulamiento)
    private String idEmpleado;
    private String nombreChofer;
    private boolean esAdmin;
    private String password; // Añadido para validación de acceso

    // 2. Constructor Vacío
    // (Esencial para librerías como Gson o Firebase al convertir JSON a Objeto)
    public CatalogoChofer() {
    }

    // 3. Constructor Completo
    public CatalogoChofer(String idEmpleado, String nombreChofer, boolean esAdmin, String password) {
        this.idEmpleado = idEmpleado;
        this.nombreChofer = nombreChofer;
        this.esAdmin = esAdmin;
        this.password = password;
    }

    // 4. Métodos de Lógica

    /**
     * Devuelve true si el usuario tiene permisos de administrador.
     */
    public boolean checkAdmin() {
        return esAdmin;
    }

    // 5. Getters y Setters
    // (Permiten leer y modificar los datos de forma segura)

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreChofer() {
        return nombreChofer;
    }

    public void setNombreChofer(String nombreChofer) {
        this.nombreChofer = nombreChofer;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}