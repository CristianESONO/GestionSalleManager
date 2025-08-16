package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Produit; // Assurez-vous d'avoir cette classe utilitaire pour les alertes
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
import java.util.UUID; // Pour générer un nom de fichier unique

public class AddProduitController {

    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private DatePicker dateLimitePicker;
    @FXML private TextField txtImagePath; // Champ de texte pour afficher le chemin de l'image
    @FXML private ImageView imageView; // ImageView pour afficher l'aperçu de l'image

    private String selectedImagePath; // Chemin absolu de l'image sélectionnée
    private ProduitController parentController; // Référence au contrôleur parent (ProduitController)

    /**
     * Définit le contrôleur parent (ProduitController) pour permettre le rafraîchissement
     * de la liste des produits après l'ajout.
     * @param parentController L'instance du ProduitController.
     */
    public void setParentController(ProduitController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image pour le produit");
        // Filtrer les extensions d'image
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage()); // Utilisez une nouvelle Stage pour le FileChooser

        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            txtImagePath.setText(selectedImagePath); // Afficher le chemin dans le TextField
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

            // Gérer le chemin de l'image : copier l'image dans un répertoire de ressources si sélectionnée
            String finalImagePath = null;
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                try {
                    // Définir le répertoire cible pour les images (par exemple, dans les ressources de l'application)
                    // Assurez-vous que ce répertoire existe ou est créé
                    Path targetDir = Paths.get("src/main/resources/com/img/produits/"); // Exemple: un sous-dossier pour les images de produits
                    if (!Files.exists(targetDir)) {
                        Files.createDirectories(targetDir);
                    }

                    File sourceFile = new File(selectedImagePath);
                    String fileExtension = "";
                    int dotIndex = selectedImagePath.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < selectedImagePath.length() - 1) {
                        fileExtension = selectedImagePath.substring(dotIndex);
                    }
                    
                    // Générer un nom de fichier unique pour éviter les conflits
                    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                    Path targetPath = targetDir.resolve(uniqueFileName);
                    
                    Files.copy(sourceFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    // Stocker le chemin relatif ou le nom du fichier pour la base de données
                    // Ici, nous stockons le chemin absolu pour la simplicité, mais un chemin relatif
                    // ou juste le nom du fichier (si les images sont dans un dossier connu) est souvent préférable.
                    finalImagePath = targetPath.toAbsolutePath().toString();

                } catch (IOException e) {
                    ControllerUtils.showErrorAlert("Erreur de copie d'image", "Impossible de copier l'image : " + e.getMessage());
                    e.printStackTrace();
                    // Continuer sans image si la copie échoue
                    finalImagePath = null; 
                }
            }

            Produit nouveauProduit = new Produit(nom, prix, stock, finalImagePath, dateLimite);
            Fabrique.getService().insertProduit(nouveauProduit);
            ControllerUtils.showInfoAlert("Succès", "Produit ajouté avec succès !");

            // Rafraîchir la liste des produits dans le contrôleur parent
            if (parentController != null) {
                parentController.refreshProduitList();
            }

            // Fermer la fenêtre d'ajout
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
