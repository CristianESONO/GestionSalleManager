package com;
import java.io.IOException;
import java.net.ServerSocket;

import com.update.UpdateDownloader;
import com.update.VersionChecker;


import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Launcher {
    private static ServerSocket lockSocket;

    public static void main(String[] args) {
        try {
            lockSocket = new ServerSocket(45678);
            // Vérifie les mises à jour avant de lancer JavaFX
            checkForUpdates();
            // Lance l'application principale
            App.main(args);
        } catch (IOException e) {
            System.out.println("L'application est déjà en cours d'exécution.");
        }
    }

    private static void checkForUpdates() {
        String currentVersion = "1.0.0";
        String lastVersion = VersionChecker.getLastVersionFromServer();
        if (lastVersion != null && !lastVersion.equals(currentVersion)) {
            Platform.startup(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Mise à jour disponible");
                alert.setHeaderText("Une nouvelle version est disponible !");
                alert.setContentText("Version actuelle : " + currentVersion + "\nDernière version : " + lastVersion);
                ButtonType buttonOui = new ButtonType("Oui");
                ButtonType buttonNon = new ButtonType("Non");
                alert.getButtonTypes().setAll(buttonOui, buttonNon);
                alert.showAndWait().ifPresent(response -> {
                    if (response == buttonOui) {
                        UpdateDownloader.downloadAndInstallUpdate(lastVersion);
                    }
                });
            });
        }
    }
}
