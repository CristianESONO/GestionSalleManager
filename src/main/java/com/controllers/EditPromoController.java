package com.controllers;

import java.time.LocalDate; // Importez LocalDate
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.core.Fabrique;
import com.entities.Promotion;
import com.entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditPromoController {

    @FXML private TextField nomField;
    @FXML private TextField tauxField;
    @FXML private CheckBox activeCheck;
    @FXML private ListView<Produit> produitsListView;
    @FXML private DatePicker dateDebutPicker; // Ajouté pour la date de début
    @FXML private DatePicker dateFinPicker;   // Ajouté pour la date de fin

    private Promotion promotion;
    private ObservableList<Produit> allProduits;

    /**
     * Appelé par la fenêtre principale pour initialiser la promotion à éditer.
     */
    public void setPromotion(Promotion promo) {
    // Load the promotion with its products in the same transaction
    this.promotion = Fabrique.getService().getPromotionByIdWithProduits(promo.getId());
    
    // Configuration de la ListView déplacée ici car 'promotion' est disponible
    produitsListView.setCellFactory(lv -> new ListCell<Produit>() {
        @Override
        protected void updateItem(Produit item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item.getNom());
        }
    });
    produitsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    
    loadData();
}

private void loadData() {
    nomField.setText(promotion.getNom());
    tauxField.setText(String.valueOf(promotion.getTauxReduction() * 100.0f)); 
    activeCheck.setSelected(promotion.isActif());
    
    // Charger les dates
    dateDebutPicker.setValue(promotion.getDateDebut());
    dateFinPicker.setValue(promotion.getDateFin());

    // Récupérer tous les produits disponibles
    allProduits = FXCollections.observableArrayList(Fabrique.getService().findAllProduits());
    produitsListView.setItems(allProduits);

    // Get associated products (already loaded in setPromotion)
    List<Produit> produitsAssocies = new ArrayList<>(promotion.getProduits());

    // Sélectionner les produits associés dans la ListView
    produitsListView.getSelectionModel().clearSelection();
    for (Produit produit : produitsAssocies) {
        int index = allProduits.indexOf(produit);
        if (index >= 0) {
            produitsListView.getSelectionModel().select(index);
        }
    }
}

    @FXML
    private void handleUpdate() {
        String nom = nomField.getText().trim();
        String tauxStr = tauxField.getText().trim();
        boolean newActiveStatus = activeCheck.isSelected();
        ObservableList<Produit> selectedProduits = produitsListView.getSelectionModel().getSelectedItems();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        // --- Validations ---
        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom de la promotion est obligatoire.");
            return;
        }

        if (tauxStr.isEmpty()) {
            showAlert("Erreur", "Le taux de réduction est obligatoire.");
            return;
        }

        float taux;
        try {
            taux = Float.parseFloat(tauxStr);
            // Convertir le pourcentage en décimal si l'entrée utilisateur est un pourcentage (0-100)
            taux = taux / 100.0f;
            if (taux < 0 || taux > 1) { // Le taux doit être entre 0 et 1 après conversion
                showAlert("Erreur", "Le taux de réduction doit être entre 0 et 100 (pourcentage).");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le taux de réduction doit être un nombre valide.");
            return;
        }
        
        if (dateDebut == null || dateFin == null) {
            showAlert("Erreur", "Les dates de début et de fin de la promotion sont obligatoires.");
            return;
        }
        if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur", "La date de début ne peut pas être postérieure à la date de fin.");
            return;
        }

        if (selectedProduits.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner au moins un produit pour la promotion.");
            return;
        }

        try {
            // Sauvegarder l'ancien état actif pour la logique d'application/retrait
            boolean oldActiveStatus = promotion.isActif();

            // Mettre à jour les propriétés de l'objet promotion
            promotion.setNom(nom);
            promotion.setTauxReduction(taux); // Taux déjà en décimal
            promotion.setActif(newActiveStatus);
            promotion.setDateDebut(dateDebut);
            promotion.setDateFin(dateFin);

            // 1. Mettre à jour les propriétés de base de la promotion
            Fabrique.getService().updatePromotion(promotion);

           // 2. Récupère les produits ACTUELS de la promotion (sans LazyInitializationException)
            List<Produit> currentProduits = Fabrique.getService().getProduitsByPromotionId(promotion.getId());
            Set<Integer> currentProduitIds = currentProduits.stream()
                .map(Produit::getId)
                .collect(Collectors.toSet());

           // 3. Récupère les IDs des produits sélectionnés
            Set<Integer> newSelectedProduitIds = selectedProduits.stream()
                .map(Produit::getId)
                .collect(Collectors.toSet());


             // 4. Identifie les produits à ajouter/retirer
            Set<Integer> produitsToAdd = newSelectedProduitIds.stream()
                .filter(id -> !currentProduitIds.contains(id))
                .collect(Collectors.toSet());

            Set<Integer> produitsToRemove = currentProduitIds.stream()
                .filter(id -> !newSelectedProduitIds.contains(id))
                .collect(Collectors.toSet());

            // 5. Applique les changements
            for (Integer produitId : produitsToAdd) {
                Fabrique.getService().addProduitToPromotion(produitId, promotion.getId());
            }

            for (Integer produitId : produitsToRemove) {
                Fabrique.getService().removeProduitFromPromotion(produitId, promotion.getId());
            }

            // 6. Gère le changement d'état actif
            if (newActiveStatus && !oldActiveStatus) {
                Fabrique.getService().appliquerPromotion(promotion);
            } else if (!newActiveStatus && oldActiveStatus) {
                Fabrique.getService().retirerPromotion(promotion);
            }
            // Si le statut ne change pas, le service a déjà géré les prix via add/removeProduitToPromotion

            showAlert("Succès", "Promotion modifiée et produits associés avec succès.");
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la modification de la promotion : " + e.getMessage());
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}