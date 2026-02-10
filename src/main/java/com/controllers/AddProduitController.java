package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

public class AddProduitController {
    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private DatePicker dateLimitePicker;
    @FXML private TextField txtImagePath;
    @FXML private ImageView imageView;
    private String selectedImagePath;
    private ProduitController parentController;

    public void setParentController(ProduitController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le produit");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            txtImagePath.setText(selectedImagePath);
            try {
                Image image = new Image(selectedFile.toURI().toURL().toExternalForm());
                imageView.setImage(image);
                imageView.setFitWidth(150);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                ControllerUtils.showErrorAlert("Erreur d'affichage de l'image", "Impossible d'afficher l'image sélectionnée.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void addProduit(ActionEvent event) {
        String nom = nomField.getText();
        String prixText = prixField.getText();
        String stockText = stockField.getText();
        LocalDate dateLimite = dateLimitePicker.getValue();
        if (nom.isEmpty() || prixText.isEmpty() || stockText.isEmpty() || dateLimite == null) {
            ControllerUtils.showErrorAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        try {
            BigDecimal prix = new BigDecimal(prixText);
            int stock = Integer.parseInt(stockText);
            if (prix.compareTo(BigDecimal.ZERO) < 0 || stock < 0) {
                ControllerUtils.showErrorAlert("Valeurs invalides", "Le prix et le stock ne peuvent pas être négatifs.");
                return;
            }

            String finalImagePath = null;
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                try {
                    // Chemin du dossier de la base de données (AppData\Roaming\GestionSalles)
                    String appDataPath = System.getenv("APPDATA");
                    Path dbDir = Paths.get(appDataPath, "GestionSalles");
                    // Sous-dossier pour les images
                    Path imagesDir = Paths.get(dbDir.toString(), "images");
                    if (!Files.exists(imagesDir)) {
                        Files.createDirectories(imagesDir);
                    }

                    File sourceFile = new File(selectedImagePath);
                    String fileExtension = "";
                    int dotIndex = selectedImagePath.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < selectedImagePath.length() - 1) {
                        fileExtension = selectedImagePath.substring(dotIndex);
                    }

                    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                    Path targetPath = imagesDir.resolve(uniqueFileName);

                    Files.copy(sourceFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // Stocker le chemin relatif par rapport au dossier de la base de données
                    finalImagePath = Paths.get("images", uniqueFileName).toString();
                } catch (IOException e) {
                    ControllerUtils.showErrorAlert("Erreur de copie d'image", "Impossible de copier l'image : " + e.getMessage());
                    e.printStackTrace();
                    finalImagePath = null;
                }
            }

            Produit nouveauProduit = new Produit(nom, prix, stock, finalImagePath, dateLimite);
            Fabrique.getService().insertProduit(nouveauProduit);
            ControllerUtils.showInfoAlert("Succès", "Produit ajouté avec succès !");

            if (parentController != null) {
                parentController.refreshProduitList();
            }

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            ControllerUtils.showErrorAlert("Format invalide", "Veuillez entrer des nombres valides pour le prix et le stock.");
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur d'ajout", "Impossible d'ajouter le produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
