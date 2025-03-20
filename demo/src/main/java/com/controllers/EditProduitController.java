package com.controllers;

import com.core.Fabrique;
import com.entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;

public class EditProduitController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private TextField txtImagePath; // TextField pour afficher le chemin de l'image
    @FXML private ImageView imageView; // ImageView pour afficher l'image
    @FXML private Button saveButton; // Bouton Enregistrer
    @FXML private Button cancelButton; // Bouton Annuler

    private String imagePath;
    private Produit produit;

    public void setProduit(Produit produit) {
        this.produit = produit;
        nomField.setText(produit.getNom());
        prixField.setText(produit.getPrix().toString());
        stockField.setText(String.valueOf(produit.getStock()));
        imagePath = produit.getImage();
        // Mettre à jour l'image si elle existe
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                txtImagePath.setText(file.getName());
            }
        }
    }

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

            // Mettre à jour imagePath
            imagePath = file.getAbsolutePath();
        }
    }

    @FXML
    private void updateProduit() {
        try {
            // Vérification que le produit est bien initialisé
            if (produit == null) {
                showAlert("Erreur", "Produit non trouvé.");
                return;
            }

            // Vérification des champs
            String nom = nomField.getText();
            String prixText = prixField.getText();
            String stockText = stockField.getText();
            String imagePathText = txtImagePath.getText();

            if (nom.isEmpty() || prixText.isEmpty() || stockText.isEmpty() || imagePathText.isEmpty()) {
                showAlert("Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Validation du prix
            BigDecimal prix;
            try {
                prix = new BigDecimal(prixText);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le prix doit être un nombre valide.");
                return;
            }

            // Validation du stock
            int stock;
            try {
                stock = Integer.parseInt(stockText);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le stock doit être un nombre entier valide.");
                return;
            }

            // Mise à jour du produit
            produit.setNom(nom);
            produit.setPrix(prix);
            produit.setStock(stock);
            produit.setImage(imagePathText);

            // Vérification que le produit existe dans la base de données avant de le mettre à jour
            if (!Fabrique.getService().checkIfProduitExists(produit.getNom())) {
                showAlert("Erreur", "Produit non trouvé dans la base de données.");
                return;
            }

            // Mise à jour du produit dans la base de données
            Fabrique.getService().updateProduit(produit);

            // Fermer la fenêtre après l'enregistrement
            closeWindow();
        } catch (Exception e) {
            // Afficher l'exception détaillée dans l'alerte et la console
            e.printStackTrace();  // Pour afficher la stack trace dans la console
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour du produit : " + e.getMessage());
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
