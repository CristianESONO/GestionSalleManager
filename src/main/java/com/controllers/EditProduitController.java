package com.controllers;

import com.core.Fabrique;
import com.entities.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import java.util.UUID; // Pour générer un nom de fichier unique

public class EditProduitController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private DatePicker dateLimitePicker;
    @FXML private TextField txtImagePath; // Champ de texte pour afficher le chemin de l'image
    @FXML private ImageView imageView; // ImageView pour afficher l'aperçu de l'image

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Produit produit;
    private ProduitController parentController; // Référence au contrôleur parent (ProduitController)

    /**
     * Définit le contrôleur parent (ProduitController) pour permettre le rafraîchissement
     * de la liste des produits après la modification.
     * @param parentController L'instance du ProduitController.
     */
    public void setParentController(ProduitController parentController) {
        this.parentController = parentController;
    }

    /**
     * Initialise la fenêtre de modification avec les données du produit.
     * @param produit L'objet Produit à modifier.
     */
    /** Résout le chemin image (relatif APPDATA ou absolu) vers un fichier. */
    private static File resolveImagePathForEdit(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;
        try {
            if (imagePath.contains(":\\") || imagePath.startsWith("/")) {
                return new File(imagePath);
            }
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                Path resolved = Paths.get(appData, "GestionSalles", imagePath.replace("\\", "/"));
                File f = resolved.toFile();
                if (f.exists()) return f;
            }
            return new File(imagePath);
        } catch (Exception e) {
            return null;
        }
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        nomField.setText(produit.getNom());
        prixField.setText(produit.getPrix().toPlainString());
        stockField.setText(String.valueOf(produit.getStock()));
        dateLimitePicker.setValue(produit.getDateLimiteConsommation());

        // Charger et afficher l'image existante (résolution du chemin APPDATA ou absolu)
        String existingImagePath = produit.getImage();
        if (existingImagePath != null && !existingImagePath.isEmpty()) {
            File file = resolveImagePathForEdit(existingImagePath);
            if (file != null && file.exists()) {
                try {
                    Image image = new Image(file.toURI().toURL().toExternalForm());
                    imageView.setImage(image);
                    txtImagePath.setText(existingImagePath);
                } catch (Exception e) {
                    ControllerUtils.showErrorAlert("Erreur d'affichage de l'image", "Impossible d'afficher l'image existante.");
                    e.printStackTrace();
                    txtImagePath.setText(existingImagePath);
                    imageView.setImage(null);
                }
            } else {
                txtImagePath.setText(existingImagePath);
                imageView.setImage(null);
            }
        } else {
            txtImagePath.setText("");
            imageView.setImage(null);
        }
    }

    @FXML
    private void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le produit");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage()); // Utilisez une nouvelle Stage pour le FileChooser

        if (selectedFile != null) {
            txtImagePath.setText(selectedFile.getAbsolutePath()); // Afficher le chemin dans le TextField
            try {
                // Afficher l'aperçu de l'image
                Image image = new Image(selectedFile.toURI().toURL().toExternalForm());
                imageView.setImage(image);
                imageView.setFitWidth(150); // Ajustez la taille de l'aperçu si nécessaire
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                ControllerUtils.showErrorAlert("Erreur d'affichage de l'image", "Impossible d'afficher l'image sélectionnée.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void updateProduit(ActionEvent event) {
        if (produit == null) {
            ControllerUtils.showErrorAlert("Erreur", "Produit non trouvé pour la modification.");
            return;
        }

        String nom = nomField.getText().trim();
        String prixText = prixField.getText().trim();
        String stockText = stockField.getText().trim();
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

            // Gérer le chemin de l'image : conserver l'ancienne ou copier la nouvelle dans APPDATA (comme à l'ajout)
            String finalImagePath = produit.getImage();
            String currentTxtImagePath = txtImagePath.getText() != null ? txtImagePath.getText().trim() : "";

            File sourceFile = currentTxtImagePath.isEmpty() ? null : new File(currentTxtImagePath);
            if (sourceFile != null && sourceFile.exists() && !currentTxtImagePath.equals(produit.getImage())) {
                try {
                    String appDataPath = System.getenv("APPDATA");
                    Path imagesDir = Paths.get(appDataPath, "GestionSalles", "images");
                    if (!Files.exists(imagesDir)) {
                        Files.createDirectories(imagesDir);
                    }
                    String fileExtension = "";
                    int dotIndex = currentTxtImagePath.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < currentTxtImagePath.length() - 1) {
                        fileExtension = currentTxtImagePath.substring(dotIndex);
                    }
                    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                    Path targetPath = imagesDir.resolve(uniqueFileName);
                    Files.copy(sourceFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    finalImagePath = Paths.get("images", uniqueFileName).toString();
                } catch (IOException e) {
                    ControllerUtils.showErrorAlert("Erreur de copie d'image", "Impossible de copier l'image : " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (currentTxtImagePath.isEmpty() && (produit.getImage() == null || produit.getImage().isEmpty())) {
                finalImagePath = null;
            }
            // Sinon : on garde produit.getImage() (déjà dans finalImagePath)

            // Mise à jour de l'objet produit
            produit.setNom(nom);
            produit.setPrix(prix);
            produit.setStock(stock);
            produit.setImage(finalImagePath);
            produit.setDateLimiteConsommation(dateLimite);

            Fabrique.getService().updateProduit(produit);
            ControllerUtils.showInfoAlert("Succès", "Produit modifié avec succès !");

            // Rafraîchir la liste des produits dans le contrôleur parent
            if (parentController != null) {
                parentController.refreshProduitList();
            }

            closeWindow();

        } catch (NumberFormatException e) {
            ControllerUtils.showErrorAlert("Format invalide", "Veuillez entrer des nombres valides pour le prix et le stock.");
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur de modification", "Impossible de modifier le produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel(ActionEvent event) { // Garder ActionEvent pour la cohérence
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
