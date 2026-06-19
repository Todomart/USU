package com.example.controllogistica;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbxXlfjoMTqxjHktJo0sDXd0k-3M0BdLYLTDybOeSaPoVkm2fc4wOS_eUR3J5ifle5PJPg/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestor = new GestorSesionPU(this);

        if (gestor.estaLogueado()) {
            irAPantalla(gestor.getRol()); // Asumo que tu gestor tiene un getRol()
            return;
        }

        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.etUsuarioLogin);
        etPass = findViewById(R.id.etPasswordLogin);
        btnEntrar = findViewById(R.id.btnIngresar);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnEntrar.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();

            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. VALIDACIÓN LOCAL (Offline)
            if (gestor.validarLogin(u, p)) {
                irAPantalla(gestor.getRol());
            }
            // 2. VALIDACIÓN ONLINE
            else if (hayConexion()) {
                validarAdminEnNube(u, p);
            }
            else {
                Toast.makeText(this, "Sin internet y no hay sesión guardada", Toast.LENGTH_LONG).show();
            }
        });

        btnIrRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void validarAdminEnNube(final String u, final String p) {
        String urlLogin = URL_WEB_APP + "?accion=loginAdmin&usuarioAdmin=" + u + "&contrasenaAdmin=" + p;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, urlLogin,
                response -> {
                    String res = response.trim();
                    if (res.contains("LOGIN_")) {
                        // Guardamos el rol en el gestor para que la próxima vez sea OFFLINE
                        String rol = res.contains("ADMIN") ? "ADMIN" : "CHOFER";
                        gestor.guardarSesion(u, p, rol);
                        irAPantalla(rol);
                    } else {
                        Toast.makeText(this, "Error: " + res, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show());

        request.setShouldCache(false);
        queue.add(request);
    }

    private void irAPantalla(String rol) {
        if ("ADMIN".equals(rol)) {
            startActivity(new Intent(this, AdminActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    private boolean hayConexion() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}