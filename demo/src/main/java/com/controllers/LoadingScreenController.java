package com.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadingScreenController {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        // Simuler un temps de chargement (optionnel)
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simuler un chargement de 3 secondes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Passer à la vue de connexion après le chargement
            javafx.application.Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/Connexion.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) progressIndicator.getScene().getWindow();
                    stage.setScene(new Scene(root));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }
}