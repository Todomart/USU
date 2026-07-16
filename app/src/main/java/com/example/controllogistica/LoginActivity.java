package com.example.controllogistica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LoginActivity extends AppCompatActivity {
    private EditText etUser, etPass;
    private Button btnEntrar, btnIrRegistro;
    private GestorSesionPU gestor;
    private final String URL = "https://script.google.com/macros/s/AKfycbyQBI0CClqrSbQOV2fMB_Cm2hrSzdKlg0lFRqBsWAzBknCBs2KKIkTPGz0N9DRWdGWkKg/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestor = new GestorSesionPU(this);

        // Lógica de navegación directa si ya hay sesión activa
        if (gestor.estaLogueado()) {
            if ("ADMIN".equals(gestor.getRol())) {
                irA(AdminActivity.class);
            } else {
                irA(MainActivity.class);
            }
            return;
        }

        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.etUsuarioLogin);
        etPass = findViewById(R.id.etPasswordLogin);
        btnEntrar = findViewById(R.id.btnIngresar);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnEntrar.setOnClickListener(v -> validarEnNube(etUser.getText().toString().trim(), etPass.getText().toString().trim()));
        btnIrRegistro.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));
    }

    private void validarEnNube(String u, String p) {
        String urlLogin = URL + "?accion=loginAdmin&usuarioAdmin=" + u + "&contrasenaAdmin=" + p;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, urlLogin, response -> {
            String res = (response != null) ? response.trim() : "";
            if (res.equals("LOGIN_ADMIN")) {
                gestor.guardarSesion(u, p, "ADMIN");
                irA(AdminActivity.class);
            } else if (res.equals("LOGIN_CHOFER")) {
                gestor.guardarSesion(u, p, "CHOFER");
                String [] datos = response.split("\\|");
                if(datos.length >= 3) {
                    String nombreReal = datos[1];
                    String unidad = datos[2];
                    SharedPreferences prefs  = getSharedPreferences("SesionChofer", MODE_PRIVATE);
                    prefs.edit().putString("nombre", nombreReal).putString("unidad" ,unidad).putBoolean("logueado", true).apply();
                }
                irA(MainActivity.class);
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            // Respaldo solo para CHÓFERES
            if (gestor.validarLogin(u, p) && "CHOFER".equals(gestor.getRol())) {
                Toast.makeText(this, "Modo Offline: Sesión de chofer recuperada", Toast.LENGTH_SHORT).show();
                irA(MainActivity.class);
            } else {
                // Si es ADMIN, no entra en modo offline. Si es otro caso, error.
                Toast.makeText(this, "Sin conexión. El escáner requiere internet.", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(request);

    }

    private void irA(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}