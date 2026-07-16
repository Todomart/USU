package com.example.controllogistica;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.util.HashSet;

public class SistemaQr {

    private static HashSet<String> codigosQuemados = new HashSet<>();
    private static final String TAG = "SistemaQrDebug";

    public Bitmap generarTicketTemporal(String nombre, String unidad) {
        // Solo nombre y unidad
        String contenido = nombre + "|" + unidad;
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 400, 400);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Log.e(TAG, "Error generando QR", e);
            return null;
        }
    }
    public static boolean validarEscaneo(String contenidoQr) {
        try {
            String contenidoLimpio = contenidoQr.trim();
            String[] partes = contenidoLimpio.split("\\|");

            Log.d(TAG, "Contenido recibido: " + contenidoLimpio);


            if (partes.length < 2) {
                return false;
            }

            String chofer = partes[0].trim();
            String unidad = partes[1].trim();


            if (chofer.equalsIgnoreCase("Sin Nombre") || unidad.equalsIgnoreCase("S/N") ||
                    chofer.isEmpty() || unidad.isEmpty()) {
                Log.e(TAG, "Dato RECHAZADO por contener valores genéricos o vacíos");
                return false;
            }


            if (codigosQuemados.contains(contenidoLimpio)) {
                Log.w(TAG, "QR ya fue utilizado");
                return false;
            }

            codigosQuemados.add(contenidoLimpio);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error en validación", e);
            return false;
        }
    }
//    public static boolean validarEscaneo(String contenidoQr) {
//        try {
//            String[] partes = contenidoQr.trim().split("\\|");
//            Log.d(TAG, "Contenido recibido: " + contenidoQr);
//
//            // Verificamos que al menos tenga Chofer y Unidad
//            if (partes.length < 2) {
//                Log.w(TAG, "Formato incorrecto. Partes: " + partes.length);
//                return false;
//            }
//
//            // Ya no validamos timestamp aquí.
//            // La validez es que el QR no se haya usado antes.
//            if (codigosQuemados.contains(contenidoQr)) {
//                Log.w(TAG, "QR ya fue utilizado");
//                return false;
//            }
//
//            codigosQuemados.add(contenidoQr);
//            return true;
//        } catch (Exception e) {
//            Log.e(TAG, "Error en validación", e);
//            return false;
//        }
//    }

    public static void limpiarCodigos() {
        codigosQuemados.clear();
    }
}