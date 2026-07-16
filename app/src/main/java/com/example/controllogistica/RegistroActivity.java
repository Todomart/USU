package com.example.controllogistica;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etUnidad, etUsuario, etPassword;
    private Button btnRegistrar;
    private GestorSesionPU gestor;
    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbyQBI0CClqrSbQOV2fMB_Cm2hrSzdKlg0lFRqBsWAzBknCBs2KKIkTPGz0N9DRWdGWkKg/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        gestor = new GestorSesionPU(this);
        etNombre = findViewById(R.id.etNombreRegistro);
        etUnidad = findViewById(R.id.etUnidadRegistro);
        etUsuario = findViewById(R.id.etUsuarioRegistro);
        etPassword = findViewById(R.id.etContraRegistro);
        btnRegistrar = findViewById(R.id.btnRegistrarChofer);


        if (btnRegistrar != null) btnRegistrar.setOnClickListener(v -> registrarEnNube());
    }


    private void registrarEnNube() {
        String nombre = etNombre.getText().toString().trim();
        String unidad = etUnidad.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || unidad.isEmpty() || usuario.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            String url = URL_WEB_APP + "?accion=registrarUsuario" +
                    "&chofer=" + java.net.URLEncoder.encode(nombre, "UTF-8") +
                    "&numCamioneta=" + java.net.URLEncoder.encode(unidad, "UTF-8") +
                    "&usuario=" + java.net.URLEncoder.encode(usuario, "UTF-8") +
                    "&contrasena=" + java.net.URLEncoder.encode(pass, "UTF-8");

            StringRequest request = new StringRequest(Request.Method.GET, url, response -> {

                if (response != null && response.trim().equalsIgnoreCase("USUARIO_GUARDADO")) {
                    gestor.guardarRegistroChofer(usuario, pass, nombre, unidad);
                    gestor.guardarSesion(usuario, pass, "CHOFER");
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Respuesta del servidor: " + response, Toast.LENGTH_LONG).show();
                }
            }, error -> {
                Toast.makeText(this, "Error de red: " + error.getMessage(), Toast.LENGTH_LONG).show();
            });

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al codificar datos", Toast.LENGTH_SHORT).show();
        }
    }
}

