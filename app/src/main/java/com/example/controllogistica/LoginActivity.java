package com.example.controllogistica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUser, etPass;
    private Button btnEntrar, btnIrRegistro;
    private GestorSesion gestor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestor = new GestorSesion(this);

        if (gestor.estaLogueado()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
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

            if (gestor.validarLoginAdmin(u, p)) {
                Intent i = new Intent(this, AdminActivity.class);
                // Ahora este método sí existe en el Gestor
                i.putExtra("RUTA_ASIGNADA", gestor.obtenerRutaAdmin(u));
                startActivity(i);
            } else if (gestor.validarLogin(u, p)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Acceso Denegado", Toast.LENGTH_SHORT).show();
            }
        });

        btnIrRegistro.setOnClickListener(v -> startActivity(new Intent(this, PantallaRegistro.class)));
    }
}