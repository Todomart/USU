package com.example.controllogistica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean peticionEnCurso = false;
    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbxXlfjoMTqxjHktJo0sDXd0k-3M0BdLYLTDybOeSaPoVkm2fc4wOS_eUR3J5ifle5PJPg/exec";

    private View overlayNegro;
    private Button btnSalir, btnOscurecer;

    private String casetaAsignada = "Sin Ruta";
    private static final Set<String> codigosRegistrados = new HashSet<>();

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String contenido = result.getText();
            if (contenido == null || peticionEnCurso) return;

            String codigoLimpio = contenido.trim();
            if (codigosRegistrados.contains(codigoLimpio)) {
                Toast.makeText(getApplicationContext(), "🛑 QR Duplicado", Toast.LENGTH_SHORT).show();
                emitirSonido(ToneGenerator.TONE_SUP_CONGESTION, 250);
                return;
            }

            codigosRegistrados.add(codigoLimpio);
            peticionEnCurso = true;
            barcodeView.pause();
            enviarANube(codigoLimpio);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        overlayNegro = findViewById(R.id.overlayNegro);
        btnSalir = findViewById(R.id.btnSalir);
        btnOscurecer = findViewById(R.id.btnOscurecer);

        btnSalir.setOnClickListener(v -> {
            new GestorSesionPU(this).cerrarSesion();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnOscurecer.setOnClickListener(v -> overlayNegro.setVisibility(View.VISIBLE));
        overlayNegro.setOnClickListener(v -> overlayNegro.setVisibility(View.GONE));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getIntent().hasExtra("RUTA_ASIGNADA")) {
            casetaAsignada = getIntent().getStringExtra("RUTA_ASIGNADA");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        }

        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(callback);
    }

    private void enviarANube(final String data) {
        try {
            String[] p = data.split("\\|");
            if (p.length < 8) {
                reestablecerEscaner("⚠️ QR incompleto", ToneGenerator.TONE_SUP_ERROR, null);
                return;
            }

            String encodedData = URLEncoder.encode(data, "UTF-8");
            String url = URL_WEB_APP + "?accion=registrar&datos=" + encodedData;

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        emitirSonidoFuerte(ToneGenerator.TONE_DTMF_S, 300);
                        vibrar(new long[]{0, 150});
                        Toast.makeText(getApplicationContext(), "✅ Registrado en Escaner", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(() -> reestablecerEscaner(null, -1, null), 1500);
                    },
                    error -> reestablecerEscaner("❌ Error de red", ToneGenerator.TONE_CDMA_HIGH_L, new long[]{0, 500})
            );
            queue.add(request);
        } catch (Exception e) {
            reestablecerEscaner("Error: " + e.getMessage(), ToneGenerator.TONE_SUP_ERROR, null);
        }
    }

    private void reestablecerEscaner(String m, int t, long[] p) {
        if (m != null) Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
        if (t != -1) emitirSonido(t, 250);
        if (p != null) vibrar(p);
        peticionEnCurso = false;
        barcodeView.resume();
    }

    private void emitirSonido(int t, int d) { try { ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70); tg.startTone(t, d); } catch (Exception e) { e.printStackTrace(); } }
    private void emitirSonidoFuerte(int t, int d) { try { ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100); tg.startTone(t, d); } catch (Exception e) { e.printStackTrace(); } }
    private void vibrar(long[] p) { try { Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE); if (v != null) v.vibrate(p, -1); } catch (Exception e) { e.printStackTrace(); } }

    @Override
    protected void onResume() { super.onResume(); barcodeView.resume(); }
    @Override
    protected void onPause() { super.onPause(); barcodeView.pause(); }
}