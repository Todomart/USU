package com.example.controllogistica;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PantallaRegistro extends AppCompatActivity {
    private EditText etNombre, etUnidad, etUsuario, etContra;
    private Button btnRegistrar;
    private GestorSesionPU gestor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        gestor = new GestorSesionPU(this);

        etNombre = findViewById(R.id.etNombreRegistro);
        etUnidad = findViewById(R.id.etUnidadRegistro);
        etUsuario = findViewById(R.id.etUsuarioRegistro);
        etContra = findViewById(R.id.etContraRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrarChofer);

        btnRegistrar.setOnClickListener(v -> {
            String nom = etNombre.getText().toString().trim();
            String uni = etUnidad.getText().toString().trim();
            String usu = etUsuario.getText().toString().trim();
            String con = etContra.getText().toString().trim();

            if (usu.isEmpty() || con.isEmpty() || nom.isEmpty()) {
                Toast.makeText(this, "Nombre, Usuario y Clave son obligatorios", Toast.LENGTH_SHORT).show();
            } else {
                // Solo enviamos 4 parámetros
                gestor.guardarRegistroChofer(usu, con, nom, uni);
                SharedPreferences prefs = getSharedPreferences("SesionChofer", MODE_PRIVATE);
                prefs.edit().putBoolean("isLoggedIn", true).apply();
                Toast.makeText(this, "¡Registrado!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}