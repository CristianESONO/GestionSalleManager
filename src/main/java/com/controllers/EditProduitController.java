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
    public void setProduit(Produit produit) {
        this.produit = produit;
        nomField.setText(produit.getNom());
        prixField.setText(produit.getPrix().toPlainString());
        stockField.setText(String.valueOf(produit.getStock()));
        dateLimitePicker.setValue(produit.getDateLimiteConsommation());

        // Charger et afficher l'image existante
        String existingImagePath = produit.getImage();
        if (existingImagePath != null && !existingImagePath.isEmpty()) {
            File file = new File(existingImagePath);
            if (file.exists()) {
                try {
                    Image image = new Image(file.toURI().toURL().toExternalForm());
                    imageView.setImage(image);
                    txtImagePath.setText(existingImagePath);
                } catch (Exception e) {
                    ControllerUtils.showErrorAlert("Erreur d'affichage de l'image", "Impossible d'afficher l'image existante.");
                    e.printStackTrace();
                    txtImagePath.setText("Erreur de chargement: " + existingImagePath);
                    imageView.setImage(null); // Effacer l'image si le chargement échoue
                }
            } else {
                txtImagePath.setText("Fichier introuvable: " + existingImagePath);
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

            // Gérer le chemin de l'image : copier l'image dans un répertoire de ressources si sélectionnée
            String finalImagePath = produit.getImage(); // Par défaut, conserver l'ancien chemin
            String currentTxtImagePath = txtImagePath.getText();

            // Si le chemin dans le TextField a changé ou si l'ancien chemin était vide
            if (currentTxtImagePath != null && !currentTxtImagePath.isEmpty() && !currentTxtImagePath.equals(produit.getImage())) {
                try {
                    Path targetDir = Paths.get("src/main/resources/com/img/produits/");
                    if (!Files.exists(targetDir)) {
                        Files.createDirectories(targetDir);
                    }

                    File sourceFile = new File(currentTxtImagePath);
                    if (!sourceFile.exists()) {
                         ControllerUtils.showErrorAlert("Image invalide", "Le fichier d'image sélectionné n'existe pas ou le chemin est incorrect.");
                         return;
                    }

                    String fileExtension = "";
                    int dotIndex = currentTxtImagePath.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < currentTxtImagePath.length() - 1) {
                        fileExtension = currentTxtImagePath.substring(dotIndex);
                    }
                    
                    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                    Path targetPath = targetDir.resolve(uniqueFileName);
                    
                    Files.copy(sourceFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    finalImagePath = targetPath.toAbsolutePath().toString();

                } catch (IOException e) {
                    ControllerUtils.showErrorAlert("Erreur de copie d'image", "Impossible de copier l'image : " + e.getMessage());
                    e.printStackTrace();
                    // Continuer avec l'ancien chemin ou sans image si la copie échoue
                    // finalImagePath reste l'ancien chemin, ou serait null si c'était le cas.
                }
            } else if (currentTxtImagePath == null || currentTxtImagePath.isEmpty()) {
                // Si le champ de texte est vidé, l'image est retirée
                finalImagePath = null;
            }

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
