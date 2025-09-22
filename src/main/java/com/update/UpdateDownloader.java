package com.update;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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
                        installUpdate(destinationPath, version); // Passer la nouvelle version
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


    private static void updateVersionInConfig(String newVersion) {
    try {
        // Chemin vers le fichier config.properties dans les ressources
        String configPath = "src/main/resources/com/config.properties";
        Path path = Paths.get(configPath);

        // Lire le fichier existant
        Properties prop = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            prop.load(input);
        }

        // Mettre à jour la version
        prop.setProperty("current_version", newVersion);

        // Écrire les modifications dans le fichier
        try (OutputStream output = Files.newOutputStream(path)) {
            prop.store(output, "Version mise à jour");
        }

    } catch (Exception e) {
        System.err.println("Erreur lors de la mise à jour de la version dans config.properties : " + e.getMessage());
    }
}


    private static void downloadUpdate(String downloadUrl, String destinationPath) throws Exception {
    try {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        // Vérifier le code de réponse HTTP
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Erreur HTTP: " + responseCode);
        }
        
        try (InputStream in = connection.getInputStream();
             FileOutputStream fos = new FileOutputStream(destinationPath)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            
            if (totalBytesRead == 0) {
                throw new Exception("Aucune donnée téléchargée");
            }
        }
    } catch (Exception e) {
        // Supprimer le fichier potentiellement corrompu
        new File(destinationPath).delete();
        throw e;
    }
}

    private static void installUpdate(String setupPath, String newVersion) throws Exception {
    // Créer un fichier de verrouillage
    String lockFilePath = System.getProperty("user.home") + "/Desktop/update_lock.txt";
    java.nio.file.Files.write(java.nio.file.Paths.get(lockFilePath), "update_in_progress".getBytes());

    // Mettre à jour la version dans config.properties
    updateVersionInConfig(newVersion);

    // Lancer le setup
    ProcessBuilder pb = new ProcessBuilder(setupPath, "/SILENT", "/NORESTART");
    pb.start();

    // Fermer l'application
    System.exit(0);
}


}
