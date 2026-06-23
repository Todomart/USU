package com.example.controllogistica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainActivity extends AppCompatActivity {
    private GestorSesionPU gestor;
    private ImageView imgQR;
    private Button btnGenerar;
    private TextView tvTimer;

    private final Handler handler = new Handler();
    private int segundosRestantes = 0;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestor = new GestorSesionPU(this);
        imgQR = findViewById(R.id.imgQRCode);
        btnGenerar = findViewById(R.id.btnGenerarQR);
        tvTimer = findViewById(R.id.tvTimer);
        Button btnCerrar = findViewById(R.id.btnCerrarSesion);

        // Agregado: Validación de seguridad para asegurar que el admin no se quede aquí
        if ("ADMIN".equals(gestor.getRol())) {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        btnGenerar.setOnClickListener(v -> {
            generarQr();
            iniciarTemporizador();
        });

        btnCerrar.setOnClickListener(v -> {
            gestor.cerrarSesion();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void generarQr() {
        try {
            String nombre = gestor.getNombre();
            String unidad = gestor.getUnidad();
            if (nombre == null || unidad == null) return;

            String data = nombre + "|" + unidad + "|" + System.currentTimeMillis();
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 500, 500);

            imgQR.setImageDrawable(null);
            imgQR.setImageBitmap(bitmap);

            btnGenerar.setEnabled(false);
            segundosRestantes = 120;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciarTemporizador() {
        if (runnable != null) handler.removeCallbacks(runnable);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (segundosRestantes > 0) {
                    segundosRestantes--;
                    tvTimer.setVisibility(android.view.View.VISIBLE);
                    tvTimer.setText("Espera " + segundosRestantes + "s para otro QR");
                    handler.postDelayed(this, 1000);
                } else {
                    tvTimer.setVisibility(android.view.View.INVISIBLE);
                    btnGenerar.setEnabled(true);
                    btnGenerar.setText("GENERAR NUEVO QR");
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}