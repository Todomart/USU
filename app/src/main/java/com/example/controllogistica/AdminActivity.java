package com.example.controllogistica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.controllogistica.data.Registro;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbyQBI0CClqrSbQOV2fMB_Cm2hrSzdKlg0lFRqBsWAzBknCBs2KKIkTPGz0N9DRWdGWkKg/exec";
    private View overlayNegro;
    private Button btnSalir, btnOscurecer;
    private boolean isPaused = false;

    private DecoratedBarcodeView barcodeScanner;
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            manejarEscaneo(result.getContents());
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        overlayNegro = findViewById(R.id.overlayNegro);
        btnSalir = findViewById(R.id.btnSalir);
        btnOscurecer = findViewById(R.id.btnOscurecer);

        btnSalir.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        });
        barcodeScanner = findViewById(R.id.barcodeScanner);

        // Configuración para que el escáner se quede siempre encendido
        barcodeScanner.decodeContinuous(result -> {
            if (result.getText() != null) {
                manejarEscaneo(result.getText());
            }
        });
        btnOscurecer.setOnClickListener(v -> {
            if (overlayNegro.getVisibility() == View.GONE) {
                overlayNegro.setVisibility(View.VISIBLE);
            } else {
                overlayNegro.setVisibility(View.GONE);
            }
        });


        overlayNegro.setOnClickListener(v -> overlayNegro.setVisibility(View.GONE));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }

    private void manejarEscaneo(String contenido) {
        if (SistemaQr.validarEscaneo(contenido)) {
            isPaused = true;
            String[] partes = contenido.split("\\|");


            Registro reg = new Registro();
            reg.Chofer = partes[0];
            reg.numCamioneta = partes[1];
            reg.fecha = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
            reg.hora = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
            reg.sincronizado = 0;

            new Thread(() -> {
                MyApplication.db.registroDao().insert(reg);


                intentarSincronizar();
            }).start();
            emitirSonidoFuerte(ToneGenerator.TONE_DTMF_S, 300);
            vibrar(new long[]{0, 150});

            new Handler(Looper.getMainLooper()).postDelayed(() -> isPaused = false, 1500);
        }
    }

    private void emitirSonidoFuerte(int t, int d) {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tg.startTone(t, d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrar(long[] p) {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(p, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intentarSincronizar() {

        RequestQueue queue = Volley.newRequestQueue(this);

        new Thread(() -> {
            List<Registro> pendientes = MyApplication.db.registroDao().getNoSincronizados();
            for (Registro reg : pendientes) {
                String url = URL_WEB_APP + "?accion=registrar&chofer=" + reg.Chofer + "&unidad=" + reg.numCamioneta;

                StringRequest request = new StringRequest(Request.Method.GET, url,
                        response -> {
                            if (response != null && response.contains("SUCCESS")) {
                                new Thread(() -> {
                                    MyApplication.db.registroDao().delete(reg);
                                }).start();
                            }
                        }, error -> {
                    Log.e("SYNC_DEBUG", "Error: " + error.getMessage());
                });
                queue.add(request); // Usamos la misma cola para todos
            }
        }).start();
    }
}