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

    // Agregamos etUsuario para mapear las 4 cajas de tu XML
    private EditText etNombre, etUnidad, etUsuario, etPassword;
    private Button btnRegistrar;
    private GestorSesionPU gestor;

    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbxXlfjoMTqxjHktJo0sDXd0k-3M0BdLYLTDybOeSaPoVkm2fc4wOS_eUR3J5ifle5PJPg/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        try {
            // 1. Inicialización del gestor de SharedPreferences
            gestor = new GestorSesionPU(this);

            // 2. Mapeo idéntico de los 4 IDs de tu archivo activity_registro.xml
            etNombre = findViewById(R.id.etNombreRegistro);
            etUnidad = findViewById(R.id.etUnidadRegistro);
            etUsuario = findViewById(R.id.etUsuarioRegistro);
            etPassword = findViewById(R.id.etContraRegistro);
            btnRegistrar = findViewById(R.id.btnRegistrarChofer);

            // 3. Asignación del Click Listener seguro
            if (btnRegistrar != null) {
                btnRegistrar.setOnClickListener(v -> registrarEnNube());
            } else {
                Toast.makeText(this, "Error: Botón 'btnRegistrarChofer' no se halló en el XML", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar componentes: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void registrarEnNube() {
        // Validación de protección contra objetos nulos
        if (etNombre == null || etUnidad == null || etUsuario == null || etPassword == null) {
            Toast.makeText(this, "Error de sistema: Campos de texto no vinculados", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String unidad = etUnidad.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // Validamos que ninguno de los 4 campos esté vacío
        if (nombre.isEmpty() || unidad.isEmpty() || usuario.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // URL final que va hacia Google Sheets
        String urlFinal = URL_WEB_APP + "?accion=registrarUsuario"
                + "&chofer=" + nombre
                + "&numCamioneta=" + unidad
                + "&contrasena=" + pass;

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.GET, urlFinal,
                    response -> {
                        String res = response.trim();
                        // ... dentro del éxito de la respuesta (response -> { ...
                        if (res.contains("USUARIO_GUARDADO") || res.contains("USUARIO_ACTUALIZADO")) {

                            // CAMBIO AQUÍ: Usa el método guardarSesion que definimos para el Login Offline
                            // Pasamos el rol como "CHOFER" explícitamente
                            gestor.guardarSesion(usuario, pass, "CHOFER");

                            // Opcionalmente, mantén tu método si necesitas guardar datos extra
                            gestor.guardarRegistroChofer(usuario, pass, nombre, unidad);

                            Toast.makeText(getApplicationContext(), "✅ Chofer guardado", Toast.LENGTH_LONG).show();
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Respuesta Servidor: " + res, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(getApplicationContext(), "Error de red: Sin conexión a Google", Toast.LENGTH_SHORT).show()
            );

            queue.add(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar el envío: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}