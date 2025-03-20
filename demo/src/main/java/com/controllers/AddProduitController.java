package com.controllers;

import com.core.Fabrique;
import com.entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView; // Importation pour ImageView
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AddProduitController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private TextField txtImagePath;
    @FXML private ImageView imageView; // Ajout de l'ImageView

    private String imagePath; // Chemin de l'image (String)

    @FXML
    private void chooseImage() {
       // Ouvrir une boîte de dialogue pour sélectionner un fichier image
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

       // Récupérer le fichier sélectionné
       File file = fileChooser.showOpenDialog(new Stage());

       if (file != null) {
            // Mettre à jour le chemin de l'image dans le champ de texte
            txtImagePath.setText(file.getAbsolutePath());

            // Mettre à jour l'image dans ImageView
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
        }
    }

    @FXML
    private void addProduit() {
        try {
            String nom = nomField.getText().trim();
            if (nom.isEmpty()) {
                showAlert("Erreur", "Le nom du produit est obligatoire.");
                return;
            }

            String imagePath = txtImagePath.getText(); // Récupérer le chemin de l'image
              // Vérifier que l'image existe réellement
            Path path = Paths.get(imagePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                showAlert( "Image invalide", "Le fichier d'image sélectionné est invalide ou n'existe pas.");
                return;
            }

            BigDecimal prix;
            try {
                prix = new BigDecimal(prixField.getText().trim());
                if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert("Erreur", "Le prix doit être un nombre positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le prix doit être un nombre valide.");
                return;
            }

            int stock;
            try {
                stock = Integer.parseInt(stockField.getText().trim());
                if (stock < 0) {
                    showAlert("Erreur", "Le stock ne peut pas être négatif.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le stock doit être un nombre entier.");
                return;
            }

            // Créer un nouveau produit
            Produit produit = new Produit(nom, prix, stock, imagePath);

            // Sauvegarder le produit dans la base de données
            Fabrique.getService().insertProduit(produit);

            // Fermer la fenêtre
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inattendue est survenue.");
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}