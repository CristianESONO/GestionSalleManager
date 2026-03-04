package com.utils;


import java.net.URL;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class NotificationUtil {

    private static MediaPlayer mediaPlayer;

    /** Joue le son d'alerte (alert.mp3). Si le fichier est absent ou illisible, émet un bip système. */
    public static void playAlertSound() {
        try {
            URL resource = NotificationUtil.class.getResource("/com/sounds/alert.mp3");
            if (resource == null) {
                System.err.println("Fichier audio non trouvé : /com/sounds/alert.mp3 — bip système utilisé.");
                playFallbackBeep();
                return;
            }
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                } catch (Exception ignored) { }
                mediaPlayer = null;
            }
            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(1.0);
            mediaPlayer.setOnError(() -> {
                System.err.println("Erreur lecture son alerte — bip système utilisé.");
                playFallbackBeep();
            });
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            playFallbackBeep();
        }
    }

    /** Bip système utilisé quand aucun fichier son n'est disponible. */
    private static void playFallbackBeep() {
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception ignored) { }
    }
}
