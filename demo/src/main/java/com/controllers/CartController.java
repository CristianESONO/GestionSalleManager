package com.controllers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Produit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CartController {

    @FXML
    private ListView<String> cartItemsList; // Référence à la liste des éléments du panier

    @FXML
    private Text prixTotalText; // Référence au texte pour afficher le prix total

    private Map<Produit, Integer> produitsDansLePanier; // Map pour stocker les produits et leur quantité

    // Méthode pour définir les produits dans le panier
    public void setProduitsDansLePanier(Map<Produit, Integer> produitsDansLePanier) {
        this.produitsDansLePanier = produitsDansLePanier;
        afficherElementsDuPanier(); // Mettre à jour la ListView
    }

    // Méthode pour afficher les éléments du panier dans la ListView
    private void afficherElementsDuPanier() {
        cartItemsList.getItems().clear(); // Effacer les éléments actuels

        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();

            // Formater l'affichage de l'élément
            String itemText = String.format("%s x%d - %.2f FCFA", produit.getNom(), quantite, produit.getPrix().doubleValue() * quantite);
            cartItemsList.getItems().add(itemText); // Ajouter l'élément à la ListView
        }
    }

    // Méthode pour fermer le panier
    @FXML
    private void closeCart() {
        // Fermer la fenêtre du panier
        Stage stage = (Stage) cartItemsList.getScene().getWindow();
        stage.close();
    }

    // Méthode pour afficher le prix total
    public void setPrixTotal(BigDecimal prixTotal) {
        // Formater le prix total avec 2 décimales
        String prixTotalFormate = String.format("Prix total : %.2f FCFA", prixTotal);
        prixTotalText.setText(prixTotalFormate);
    }

    @FXML
    private void handlePayer() {
        try {
            // Charger le fichier FXML de la fenêtre de paiement
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/PaymentWindow.fxml"));
            Parent paymentPane = loader.load();

            // Obtenir le contrôleur de la fenêtre de paiement
            PaymentController paymentController = loader.getController();

            // Passer les données nécessaires (clients, montant total, détails des produits)
            List<Client> clients = Fabrique.getService().getAllClients(); // Récupérer la liste des clients
            double montantTotal = calculerPrixTotal();
            String detailsProduits = getDetailsProduits(); // Méthode pour obtenir les détails des produits

            paymentController.initializeData(clients, montantTotal, detailsProduits, produitsDansLePanier);

            // Créer une nouvelle scène pour la fenêtre de paiement
            Scene paymentScene = new Scene(paymentPane);

            // Créer une nouvelle fenêtre (Stage) pour la fenêtre de paiement
            Stage paymentStage = new Stage();
            paymentStage.initModality(Modality.APPLICATION_MODAL); // Empêcher l'interaction avec la fenêtre principale
            paymentStage.setTitle("Paiement");
            paymentStage.setScene(paymentScene);

            // Afficher la fenêtre de paiement
            paymentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour calculer le prix total
    private double calculerPrixTotal() {
        double prixTotal = 0;
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            prixTotal += produit.getPrix().doubleValue() * quantite;
        }
        return prixTotal;
    }

    // Méthode pour obtenir les détails des produits
    private String getDetailsProduits() {
        StringBuilder details = new StringBuilder();
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            details.append(produit.getNom()).append(" x").append(quantite).append(", ");
        }
        return details.toString();
    }
}