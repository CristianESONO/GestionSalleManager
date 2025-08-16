package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Launcher {
    private static ServerSocket lockSocket;

    public static void main(String[] args) {
        // 🔐 Empêche les instances multiples
        try {
            lockSocket = new ServerSocket(45678);
        } catch (IOException e) {
            System.out.println("L'application est déjà en cours d'exécution.");
            return;
        }

        // 🔄 Vérification des mises à jour
        checkForUpdates();

        // ▶️ Lance l'application
        App.main(args);
    }

    private static void checkForUpdates() {
        String currentVersion = "1.0.0"; // À remplacer par la lecture de config.properties
        String lastVersion = getLastVersionFromServer();

        if (lastVersion != null && !lastVersion.equals(currentVersion)) {
            // Utiliser Platform.runLater pour interagir avec JavaFX depuis un thread non-JavaFX
            Platform.startup(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Mise à jour disponible");
                alert.setHeaderText("Une nouvelle version est disponible !");
                alert.setContentText("Version actuelle : " + currentVersion + "\nDernière version : " + lastVersion +
                                    "\nVoulez-vous télécharger la mise à jour maintenant ?");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        downloadAndInstallUpdate(lastVersion);
                    }
                });
            });
        }
    }

    private static String getLastVersionFromServer() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/CristianESONO/GestionSalleManager/main/last_version.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la version distante : " + e.getMessage());
            return null;
        }
    }

    private static void downloadAndInstallUpdate(String version) {
        String downloadUrl = "https://github.com/CristianESONO/GestionSalleManager/releases/download/v" + version + "/GestionSalle_Setup_" + version + ".exe";
        String destinationPath = "C:\\Users\\HP\\Desktop\\GestionSalle_Setup_latest.exe";

        try {
            downloadUpdate(downloadUrl, destinationPath);
            installUpdate(destinationPath);
        } catch (Exception e) {
            System.err.println("Erreur lors du téléchargement ou de l'installation : " + e.getMessage());
        }
    }

    private static void downloadUpdate(String downloadUrl, String destinationPath) throws Exception {
        try (java.io.InputStream in = new URL(downloadUrl).openStream();
             java.nio.channels.ReadableByteChannel rbc = java.nio.channels.Channels.newChannel(in);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(destinationPath)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    private static void installUpdate(String setupPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(setupPath, "/SILENT", "/NORESTART");
        pb.start();
        System.exit(0);
    }
}
