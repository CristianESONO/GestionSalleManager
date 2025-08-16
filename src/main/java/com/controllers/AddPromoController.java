package com.controllers;

import com.core.Fabrique;
import com.entities.Produit;
import com.entities.Promotion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class AddPromoController {

    @FXML private TextField nomField;
    @FXML private TextField tauxField;
    @FXML private CheckBox activeCheck;
    @FXML private ListView<Produit> produitsListView;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;

    @FXML
    private void initialize() {
        // Configure the ListView to display the product name
        produitsListView.setCellFactory(lv -> new ListCell<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNom());
            }
        });

        // Load all products into the ListView (multiple selection)
        List<Produit> produits = Fabrique.getService().findAllProduits();
        ObservableList<Produit> observableProduits = FXCollections.observableArrayList(produits);
        produitsListView.setItems(observableProduits);
        produitsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void handleAdd() {
        String nom = nomField.getText().trim();
        String tauxStr = tauxField.getText().trim();
        boolean active = activeCheck.isSelected();
        ObservableList<Produit> selectedProduits = produitsListView.getSelectionModel().getSelectedItems();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        // --- Validations ---
        if (nom.isEmpty()) {
            showAlert("Erreur de validation", "Le nom de la promotion est obligatoire.");
            return;
        }

        if (tauxStr.isEmpty()) {
            showAlert("Erreur de validation", "Le taux de réduction est obligatoire.");
            return;
        }

        float taux;
        try {
            taux = Float.parseFloat(tauxStr);
            // Convert percentage (e.g., 10 for 10%) to decimal (0.10) for internal use
            taux = taux / 100.0f;

            if (taux < 0 || taux > 1) { // The rate must be between 0 and 1 after conversion
                showAlert("Erreur de validation", "Le taux de réduction doit être entre 0 et 100 (pourcentage).");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de validation", "Le taux de réduction doit être un nombre valide.");
            return;
        }

        if (dateDebut == null || dateFin == null) {
            showAlert("Erreur de validation", "Les dates de début et de fin de la promotion sont obligatoires.");
            return;
        }
        if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur de validation", "La date de début ne peut pas être postérieure à la date de fin.");
            return;
        }
        
        // Optional: Check if promotion name already exists
        if (Fabrique.getService().getPromotionByNom(nom).isPresent()) {
            showAlert("Erreur de validation", "Une promotion avec ce nom existe déjà. Veuillez choisir un nom unique.");
            return;
        }

        if (selectedProduits.isEmpty()) {
            showAlert("Erreur de validation", "Veuillez sélectionner au moins un produit pour la promotion.");
            return;
        }

        try {
            // 1. Create the Promotion object
            Promotion promo = new Promotion();
            promo.setNom(nom);
            promo.setTauxReduction(taux);
            promo.setActif(active);
            promo.setDateDebut(dateDebut);
            promo.setDateFin(dateFin);

            // 2. Add the promotion to the database.
            // It's highly recommended that your addPromotion service method
            // returns the Promotion object with its newly generated ID.
            // This avoids a separate lookup by name immediately after insertion.
            Promotion insertedPromo = Fabrique.getService().addPromotion(promo);

            // If addPromotion doesn't return the object with ID, you'd use getPromotionByNom,
            // but ensure your service guarantees uniqueness by name or handles potential race conditions.
            // For now, keeping your original lookup if addPromotion only returns void or a boolean.
            // Promotion newPromo = Fabrique.getService().getPromotionByNom(nom)
            //                                    .orElseThrow(() -> new Exception("Promotion non trouvée après insertion."));


            // 3. Link the selected products to the newly created promotion
            for (Produit produit : selectedProduits) {
                // If insertedPromo has the ID, use it directly
                // Otherwise, use newPromo if you uncommented the lookup
                Fabrique.getService().addProduitToPromotion(produit.getId(), insertedPromo.getId());
            }

            // 4. If the promotion is active, apply it.
            // The service will handle price application on associated products.
            if (active) {
                Fabrique.getService().appliquerPromotion(insertedPromo); // Use the object with its ID
            }

            showAlert("Succès", "Promotion ajoutée et produits associés avec succès.");
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout de la promotion : " + e.getMessage());
            e.printStackTrace();
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Changed to INFORMATION for general messages
        if (title.contains("Erreur")) { // Keep ERROR type for actual errors
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}