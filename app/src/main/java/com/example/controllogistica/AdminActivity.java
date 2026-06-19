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
import android.view.WindowManager;
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

import java.util.HashSet;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean peticionEnCurso = false;
    private final String URL_WEB_APP = "https://script.google.com/macros/s/AKfycbxXlfjoMTqxjHktJo0sDXd0k-3M0BdLYLTDybOeSaPoVkm2fc4wOS_eUR3J5ifle5PJPg/exec";

    // 🔑 Variable para amarrar la caseta que inició sesión
    private String casetaAsignada = "Sin Ruta";

    // Memoria del teléfono para guardar los QR de la sesión y evitar duplicados
    private static final Set<String> codigosRegistrados = new HashSet<>();

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String contenido = result.getText();

            if (contenido == null || peticionEnCurso) {
                return;
            }

            String codigoLimpio = contenido.trim();

            // 🛑 Si ya está en la memoria del teléfono, alerta inmediata
            if (codigosRegistrados.contains(codigoLimpio)) {
                Toast.makeText(getApplicationContext(), "🛑 QR Duplicado: Ya fue escaneado", Toast.LENGTH_SHORT).show();
                emitirSonido(ToneGenerator.TONE_SUP_CONGESTION, 250);
                return;
            }

            // 💾 Guardar en memoria local, avisar lectura y procesar
            codigosRegistrados.add(codigoLimpio);
            Toast.makeText(getApplicationContext(), "📸 Código leído, procesando...", Toast.LENGTH_SHORT).show();

            peticionEnCurso = true;
            barcodeView.pause(); // Pausa momentánea para evitar ráfagas de red

            enviarANube(codigoLimpio);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Mantener la pantalla encendida mientras se usa el escáner
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 📥 Capturamos la caseta que nos heredó LoginActivity tras iniciar sesión correctamente
        if (getIntent().hasExtra("RUTA_ASIGNADA")) {
            casetaAsignada = getIntent().getStringExtra("RUTA_ASIGNADA");
        }

        // Validación y solicitud de permisos de la cámara
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        }

        // Inicialización directa del componente de la cámara de ZXing
        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        barcodeView.setStatusText("");
        barcodeView.decodeContinuous(callback);
    }

    private void enviarANube(final String data) {
        try {
            String[] p = data.split("\\|");
            if (p.length < 2) { // El QR trae Nombre|Unidad
                reestablecerEscaner("⚠️ QR no válido", ToneGenerator.TONE_SUP_ERROR, null);
                return;
            }

            String chofer       = p[0];
            String numCamioneta = p[1];

            // 🏢 Usamos la caseta autenticada en el Login
            String ruta         = casetaAsignada;

            // El Servidor en Sheets calculará de forma automática las vueltas, fecha y hora reales
            String vueltas      = "registrado";
            String fecha        = "registrado";
            String hora         = "registrado";

            // 🚨 CORRECCIÓN: Ajustamos "accion=escanerViaje" para que el script procese el viaje libremente
            String url = URL_WEB_APP + "?accion=escanerViaje"
                    + "&chofer=" + chofer.replace(" ", "%20")
                    + "&numCamioneta=" + numCamioneta
                    + "&ruta=" + ruta.replace(" ", "%20")
                    + "&vueltas=" + vueltas
                    + "&fecha=" + fecha
                    + "&hora=" + hora;

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        // Sonido fuerte de éxito al registrar en Excel
                        emitirSonidoFuerte(ToneGenerator.TONE_DTMF_S, 300);
                        vibrar(new long[]{0, 150});
                        Toast.makeText(getApplicationContext(), "✅ Registrado en Excel", Toast.LENGTH_SHORT).show();

                        // Esperamos 1.5 segundos para dar tiempo al cambio de chofer y reactivamos la cámara
                        new Handler().postDelayed(() -> reestablecerEscaner(null, -1, null), 1500);
                    },
                    error -> {
                        reestablecerEscaner("❌ Error de red", ToneGenerator.TONE_CDMA_HIGH_L, new long[]{0, 500});
                    }
            );
            queue.add(request);

        } catch (Exception e) {
            reestablecerEscaner("Error: " + e.getMessage(), ToneGenerator.TONE_SUP_ERROR, null);
        }
    }

    private void reestablecerEscaner(String mensajeToast, int tipoTono, long[] patronVibracion) {
        if (mensajeToast != null) {
            Toast.makeText(getApplicationContext(), mensajeToast, Toast.LENGTH_SHORT).show();
        }
        if (tipoTono != -1) {
            emitirSonido(tipoTono, 250);
        }
        if (patronVibracion != null) {
            vibrar(patronVibracion);
        }
        peticionEnCurso = false;
        barcodeView.resume(); // Reanuda la cámara para el siguiente QR libre
    }

    private void emitirSonido(int tipoTono, int duracion) {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70);
            tg.startTone(tipoTono, duracion);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void emitirSonidoFuerte(int tipoTono, int duracion) {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tg.startTone(tipoTono, duracion);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void vibrar(long[] patron) {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null) v.vibrate(patron, -1);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}