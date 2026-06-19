package com.example.controllogistica;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RecuperarActivity extends AppCompatActivity {

    private TextView tvPregunta;
    private EditText etRespuesta;
    private Button btnRecuperar;
    private GestorSesionPU gestor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar);

        gestor = new GestorSesionPU(this);

        tvPregunta = findViewById(R.id.tvPreguntaSeguridad);
        etRespuesta = findViewById(R.id.etRespuestaSeguridad);
        btnRecuperar = findViewById(R.id.btnValidarRecuperacion);

        tvPregunta.setText(gestor.getPregunta());

        btnRecuperar.setOnClickListener(v -> {
            String resp = etRespuesta.getText().toString().trim();
            if (resp.equalsIgnoreCase(gestor.getRespuesta())) {
                Toast.makeText(this, "Tu clave es: " + gestor.getPassword(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Respuesta incorrecta", Toast.LENGTH_SHORT).show();
            }
        });
    }
}