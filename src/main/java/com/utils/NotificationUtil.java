package com.utils;


import java.net.URL;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class NotificationUtil {

    private static MediaPlayer mediaPlayer;

    public static void playAlertSound() {
        try {
            URL resource = NotificationUtil.class.getResource("/sounds/alert.mp3");
            if (resource == null) {
                System.err.println("Fichier audio non trouvé !");
                return;
            }
            Media media = new Media(resource.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
