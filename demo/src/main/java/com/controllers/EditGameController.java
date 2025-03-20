package com.controllers;

import com.core.Fabrique;
import com.entities.Game;
import com.services.Service;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class EditGameController {

    private Game game;

    @FXML
    private TextField txtNom, txtType, txtImagePath, txtDescription, txtStatus;

    // Méthode pour pré-remplir les champs avec les données du jeu existant
    public void setGame(Game game) {
        this.game = game;
        txtNom.setText(game.getName());
        txtType.setText(game.getType());
        txtImagePath.setText(game.getImagePath());
        txtDescription.setText(game.getDescription());
        txtStatus.setText(game.getStatus());
    }

    @FXML
    private void editGame() {
        try {
            // Récupérer les valeurs modifiées
            game.setName(txtNom.getText());
            game.setType(txtType.getText());
            game.setImagePath(txtImagePath.getText());
            game.setDescription(txtDescription.getText());
            game.setStatus(txtStatus.getText());

            // Mettre à jour le jeu via le service
            Fabrique.getService().updateGame(game);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @FXML
    private void cancel() {
        // Fermer la fenêtre actuelle
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAddImage() {
        // Ouvrir un FileChooser pour sélectionner un fichier d'image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Obtenir le fichier choisi par l'utilisateur
        File file = fileChooser.showOpenDialog(new Stage());
        
        if (file != null) {
            // Mettre à jour le champ txtImagePath avec le chemin du fichier sélectionné
            txtImagePath.setText(file.getAbsolutePath());
        }
    }
}
