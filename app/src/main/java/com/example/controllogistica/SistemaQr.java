package com.example.controllogistica;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.HashSet;

public class SistemaQr {

    // Lista estática para que persista durante la ejecución de la App
    private static HashSet<String> codigosQuemados = new HashSet<>();

    /**
     *  este es el método que genera el QR con 3 parámetros.
     * Si en el MainActivity sale rojo, es porque aquí faltaba alguno.
     */
    public Bitmap generarTicketTemporal(String nombre, String unidad, String ruta) {
        // 1. Sello de tiempo (Timestamp) para la validez de 2 minutos
        long tiempoActual = System.currentTimeMillis();

        // 2. Estructura del contenido: "Nombre|Unidad|Ruta|Tiempo"
        String contenido = nombre + "|" + unidad + "|" + ruta + "|" + tiempoActual;

        // 3. Generación física del QR
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Valida si el código escaneado es auténtico y no ha expirado.
     */
    public boolean validarEscaneo(String contenidoQr) {
        try {
            String[] partes = contenidoQr.split("\\|");
            if (partes.length < 4) return false;

            long timestampQr = Long.parseLong(partes[3]);
            long tiempoActual = System.currentTimeMillis();

            // Verificación de los 2 minutos (120,000 ms)
            if (tiempoActual - timestampQr > 120000) {
                return false; // Expirado
            }

            // Verificación de duplicados
            if (codigosQuemados.contains(contenidoQr)) {
                return false; // Ya se usó
            }

            codigosQuemados.add(contenidoQr);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Limpia la memoria de códigos (Útil para el reinicio mensual)
     */
    public void reiniciarProduccion() {
        codigosQuemados.clear();
    }
}