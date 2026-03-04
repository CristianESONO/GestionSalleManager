package com.controllers;

import javafx.fxml.FXML;

/**
 * Écran de chargement affiché au démarrage pendant la vérification des mises à jour.
 * La transition vers l'écran de connexion (ou la boîte de mise à jour) est gérée par {@link com.App}.
 */
public class LoadingScreenController {

    @FXML
    public void initialize() {
        // Affichage uniquement ; le passage à la vue suivante est piloté par App
    }
}
