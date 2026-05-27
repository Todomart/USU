package com.example.controllogistica;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.HashSet;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private Set<String> codigosRegistrados = new HashSet<>();
    private boolean peticionEnCurso = false;
    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbxXlfjoMTqxjHktJo0sDXd0k-3M0BdLYLTDybOeSaPoVkm2fc4wOS_eUR3J5ifle5PJPg/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. MODO FULL NEGRO (OCULTAR BARRAS) ---
        configurarModoInmersivo();

        setContentView(R.layout.activity_admin);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        }

        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(callback);
    }

    // Método para ocultar barra de arriba y abajo
    private void configurarModoInmersivo() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Oculta barra de abajo
                | View.SYSTEM_UI_FLAG_FULLSCREEN    // Oculta barra de arriba
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // Permite salir deslizando
        decorView.setSystemUiVisibility(uiOptions);
    }

    // Re-aplicar si el usuario sale y regresa a la app
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            configurarModoInmersivo();
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String contenido = result.getText();
            if (peticionEnCurso) return;
            if (codigosRegistrados.contains(contenido)) {
                emitirSonido(ToneGenerator.TONE_SUP_CONGESTION, 150);
                return;
            }
            peticionEnCurso = true;
            enviarANube(contenido);
        }
    };

    // Reemplaza tus métodos de sonido y envío por estos:

    private void enviarANube(final String data) {
        try {
            String[] p = data.split("\\|");
            if (p.length >= 6) {
                String url = URL_WEB_APP + "?chofer=" + p[0] + "&numCamioneta=" + p[1] +
                        "&ruta=" + p[2] + "&numVueltas=" + p[3] +
                        "&fecha=" + p[4] + "&hora=" + p[5];

                RequestQueue queue = Volley.newRequestQueue(this);
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        response -> {
                            String r = response.trim();
                            if (r.equalsIgnoreCase("DUPLICADO")) {
                                // SONIDO DE ERROR FUERTE (Tono de congestión crítico)
                                emitirSonidoFuerte(ToneGenerator.TONE_SUP_ERROR, 800);
                                vibrar(new long[]{0, 300, 200, 300, 200, 300});
                            } else {
                                codigosRegistrados.add(data);
                                // SONIDO DE ÉXITO FUERTE (Tono de confirmación alto)
                                emitirSonidoFuerte(ToneGenerator.TONE_DTMF_S, 300);
                                vibrar(new long[]{0, 150});
                            }
                            peticionEnCurso = false;
                        },
                        error -> {
                            // SONIDO DE FALLO DE RED (Tono de sirena corto)
                            emitirSonidoFuerte(ToneGenerator.TONE_CDMA_HIGH_L, 1000);
                            vibrar(new long[]{0, 1000});
                            peticionEnCurso = false;
                        }
                );
                queue.add(request);
            }
        } catch (Exception e) { peticionEnCurso = false; }
    }

    private void emitirSonidoFuerte(int tipoTono, int duracion) {
        try {
            // Usamos STREAM_ALARM para el máximo volumen posible del hardware
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tg.startTone(tipoTono, duracion);

            // Liberar el recurso después de usarlo para evitar que se sature
            new android.os.Handler().postDelayed(tg::release, duracion + 100);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void emitirSonido(int tipoTono, int duracion) {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85);
            tg.startTone(tipoTono, duracion);
        } catch (Exception ignored) {}
    }

    private void vibrar(long[] patron) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(patron, -1));
            } else {
                v.vibrate(patron, -1);
            }
        }
    }

    @Override
    protected void onResume() { super.onResume(); barcodeView.resume(); }
    @Override
    protected void onPause() { super.onPause(); barcodeView.pause(); }
}