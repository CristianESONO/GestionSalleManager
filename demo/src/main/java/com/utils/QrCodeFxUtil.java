package com.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

/**
 * Génère une image QR (JavaFX) à partir d'une chaîne (URL, payload, etc.).
 */
public final class QrCodeFxUtil {

    private QrCodeFxUtil() {}

    /**
     * @param data texte à encoder (ex. URL Wave / Orange)
     * @param size taille en pixels (carré)
     */
    public static Image encodeQr(String data, int size) throws WriterException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Données QR vides");
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints);

        int w = matrix.getWidth();
        int h = matrix.getHeight();
        WritableImage img = new WritableImage(w, h);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
                pw.setColor(x, y, c);
            }
        }
        return img;
    }
}
