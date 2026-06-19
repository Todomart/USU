package com.example.controllogistica;

import android.content.Context;
import android.content.SharedPreferences;

public class GestorSesionPU {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public GestorSesionPU(Context context) {
        prefs = context.getSharedPreferences("LogisticaPrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
        // Datos del Administrador Maestro
        editor.putString("ADMIN_admin", "1234");
        editor.putString("RUTA_admin", "RUTA GENERAL");
        editor.apply();
    }

    // --- NUEVO: MÉTODO QUE REQUERÍA LOGINACTIVITY ---
    public void guardarSesion(String usuario, String pass, String rol) {
        editor.putString("USER_ACTUAL", usuario); // Para mantener sesión
        editor.putString(usuario, pass);          // Para validarLogin()
        editor.putString("ROL_KEY", rol);         // Para distinguir Admin vs Chofer
        editor.apply();
    }

    // --- MÉTODOS EXISTENTES ---
    public String getRol() {
        return prefs.getString("ROL_KEY", "CHOFER");
    }

    public void guardarAdminLocal(String usuario, String pass, String ruta) {
        editor.putString("ADMIN_" + usuario, pass);
        editor.putString("RUTA_" + usuario, ruta);
        editor.apply();
    }

    public void guardarRegistroChofer(String usuario, String pass, String nombre, String unidad) {
        editor.putString(usuario, pass);
        editor.putString("NOMBRE_" + usuario, nombre);
        editor.putString("UNIDAD_" + usuario, unidad);
        editor.apply();
    }

    public boolean validarLogin(String usuario, String pass) {
        String p = prefs.getString(usuario, null);
        if (p != null && p.equals(pass)) {
            editor.putString("USER_ACTUAL", usuario);
            editor.apply();
            return true;
        }
        return false;
    }

    public boolean validarLoginAdmin(String u, String p) {
        String passAdmin = prefs.getString("ADMIN_" + u, null);
        return passAdmin != null && passAdmin.equals(p);
    }

    public String obtenerRutaAdmin(String u) {
        return prefs.getString("RUTA_" + u, "Sin Ruta");
    }

    public boolean estaLogueado() {
        return prefs.contains("USER_ACTUAL");
    }

    public void cerrarSesion() {
        editor.remove("USER_ACTUAL");
        editor.apply();
    }

    public String getNombre() {
        return prefs.getString("NOMBRE_" + prefs.getString("USER_ACTUAL", ""), "Sin Nombre");
    }

    public String getUnidad() {
        return prefs.getString("UNIDAD_" + prefs.getString("USER_ACTUAL", ""), "S/N");
    }

    public String getPregunta() { return "¿Nombre de tu mascota?"; }
    public String getRespuesta() { return "admin"; }
    public String getPassword() {
        String user = prefs.getString("USER_ACTUAL", "");
        return prefs.getString(user, "1234");
    }
}