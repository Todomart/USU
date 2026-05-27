package com.example.controllogistica;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etUnidad, etPassword;
    private Button btnRegistrar;

    // LA TEVA URL OFICIAL
    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbxXlfjoMTqxjHktJo0sDXd0k-3M0BdLYLTDybOeSaPoVkm2fc4wOS_eUR3J5ifle5PJPg/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombre = findViewById(R.id.etNombreRegistro);
        etUnidad = findViewById(R.id.etUnidadRegistro);
        etPassword = findViewById(R.id.etContraRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrarChofer);

        btnRegistrar.setOnClickListener(v -> registrarEnNube());
    }

    private void registrarEnNube() {
        String nombre = etNombre.getText().toString().trim();
        String unidad = etUnidad.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || unidad.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa tots els camps", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enviem el paràmetre accion=registrarUsuario per anar a la fulla "Usuarios"
        String urlFinal = URL_WEB_APP + "?accion=registrarUsuario"
                + "&chofer=" + nombre
                + "&numCamioneta=" + unidad
                + "&contrasena=" + pass;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, urlFinal,
                response -> {
                    String res = response.trim();
                    if (res.contains("USUARIO_GUARDADO") || res.contains("USUARIO_ACTUALIZADO")) {
                        Toast.makeText(this, "✅ Xofer guardat a la base de dades", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + res, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de xarxa", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
}