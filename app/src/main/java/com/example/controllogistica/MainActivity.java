package com.example.controllogistica;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainActivity extends AppCompatActivity {
    private GestorSesionPU gestor;
    private ImageView imgQR;
    private TextView tvTimer, tvSaludo;
    private Button btnGenerar, btnCerrar;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestor = new GestorSesionPU(this);
        imgQR = findViewById(R.id.imgQRCode);
        tvTimer = findViewById(R.id.tvTimer);
        tvSaludo = findViewById(R.id.tvSaludoChofer);
        btnGenerar = findViewById(R.id.btnGenerarQR);
        btnCerrar = findViewById(R.id.btnCerrarSesion);

        tvSaludo.setText("Bienvenido: " + gestor.getNombre());

        btnGenerar.setOnClickListener(v -> generarQR());
        btnCerrar.setOnClickListener(v -> {
            gestor.cerrarSesion();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void generarQR() {
        // 1. Jalamos los datos de identidad guardados en el teléfono del chofer
        String nombreChofer = gestor.getNombre().trim();
        String unidadCamion = gestor.getUnidad().trim();

        // 2. Estructura Pura de la Opción A: Nombre | Unidad
        // Quitamos la fecha, la hora rota y la ruta fija para que el QR sea ultra rápido de leer
        String data = nombreChofer + "|" + unidadCamion;

        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            imgQR.setImageBitmap(encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 500, 500));

           // Contador de tiempo para que el QR expire
            iniciarContador();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciarContador() {
        if (timer != null) timer.cancel();
        btnGenerar.setEnabled(false);
        tvTimer.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(120000, 1000) {
            public void onTick(long ms) { tvTimer.setText("Vence en: " + (ms/1000) + "s"); }
            public void onFinish() {
                imgQR.setImageBitmap(null);
                btnGenerar.setEnabled(true);
                tvTimer.setText("CÓDIGO EXPIRADO");
            }
        }.start();
    }
}