package com.update;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class UpdateDownloader {
    public static void downloadAndInstallUpdate(String version) {
        String downloadUrl = "https://github.com/CristianESONO/GestionSalleManager/releases/download/v" + version + "/GestionSalle_Setup_" + version + ".exe";
        String destinationPath = System.getProperty("user.home") + "/Desktop/GestionSalle_Setup_latest.exe";

        Platform.runLater(() -> {
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Téléchargement...");
            progressAlert.setHeaderText(null);
            progressAlert.setContentText("Mise à jour en cours de téléchargement...");
            progressAlert.show();

            new Thread(() -> {
                try {
                    downloadUpdate(downloadUrl, destinationPath);
                    Platform.runLater(() -> {
                        progressAlert.close();
                        try {
                            installUpdate(destinationPath);
                        } catch (Exception e) {
                            new Alert(Alert.AlertType.ERROR, "Échec de l'installation : " + e.getMessage()).show();
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        progressAlert.close();
                        new Alert(Alert.AlertType.ERROR, "Échec du téléchargement : " + e.getMessage()).show();
                    });
                }
            }).start();
        });
    }

    private static void downloadUpdate(String downloadUrl, String destinationPath) throws Exception {
        try (InputStream in = new URL(downloadUrl).openStream();
             ReadableByteChannel rbc = Channels.newChannel(in);
             FileOutputStream fos = new FileOutputStream(destinationPath)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    private static void installUpdate(String setupPath) throws Exception {
        // Créer un fichier de verrouillage
        String lockFilePath = System.getProperty("user.home") + "/Desktop/update_lock.txt";
        java.nio.file.Files.write(java.nio.file.Paths.get(lockFilePath), "update_in_progress".getBytes());

        // Lancer le setup
        ProcessBuilder pb = new ProcessBuilder(setupPath, "/SILENT", "/NORESTART");
        pb.start();

        // Fermer l'application
        System.exit(0);
    }

}
