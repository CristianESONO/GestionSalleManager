package com.controllers;

import com.core.Fabrique;
import com.entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProduitController {

    @FXML
    private GridPane gridProduits; // Grille pour afficher les produits

    @FXML
    private Button btnAjouterProduit; // Bouton pour ajouter un produit

    @FXML
    private ImageView cartIcon; // Icône du panier

    @FXML
    private Text cartCounter; // Compteur du panier

    private List<Produit> produits; // Liste des produits
    private int currentPage = 0; // Page actuelle
    private final int COLUMNS_PER_PAGE = 3; // Nombre de colonnes par page
    private final int ROWS_PER_COLUMN = 3; // Nombre de lignes par colonne

    private int cartCount = 0; // Compteur du panier
    private Map<Produit, Integer> produitsDansLePanier = new HashMap<>(); // Map pour stocker les produits et leur quantité

    @FXML
    public void initialize() {
        // Chargement des produits au démarrage
        produits = Fabrique.getService().findAllProduits();
        displayPage(currentPage); // Afficher la première page

        // Initialiser le compteur du panier
        cartCounter.setText(String.valueOf(cartCount));
    }

    // Afficher une page spécifique
    private void displayPage(int page) {
        gridProduits.getChildren().clear(); // Effacer les cartes actuelles

        int startIndex = page * COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int endIndex = Math.min(startIndex + (COLUMNS_PER_PAGE * ROWS_PER_COLUMN), produits.size());

        int row = 0;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Produit produit = produits.get(i);

            // Créer une carte pour le produit
            HBox produitCard = createProduitCard(produit);

            // Ajouter la carte au GridPane
            gridProduits.add(produitCard, col, row);

            // Passer à la ligne suivante après 3 cartes
            row++;
            if (row >= ROWS_PER_COLUMN) {
                row = 0;
                col++;
            }
        }
    }

    // Créer une carte pour un produit
    private HBox createProduitCard(Produit produit) {
        HBox produitCard = new HBox(10);
        produitCard.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-padding: 10;");

        // Image du produit
        ImageView imageView = new ImageView(produit.getImage());
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        // Nom, prix et boutons
        VBox infoBox = new VBox(10);
        Text produitName = new Text(produit.getNom());
        produitName.setStyle("-fx-font-size: 18px; -fx-fill: black;");

        Text produitPrix = new Text("Prix: " + produit.getPrix() + " FCFA");
        produitPrix.setStyle("-fx-font-size: 16px; -fx-fill: grey;");

        HBox buttonBox = new HBox(10);
        Button btnAjouter = new Button("Ajouter au panier");
        btnAjouter.setStyle("-fx-background-color: rgb(8, 96, 134); -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnAjouter.setOnAction(event -> ajouterAuPanier(produit));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #B0BEC5; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnModifier.setOnAction(event -> modifierProduit(produit));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnSupprimer.setOnAction(event -> {
            try {
                supprimerProduit(produit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonBox.getChildren().addAll(btnAjouter, btnModifier, btnSupprimer);
        infoBox.getChildren().addAll(produitName, produitPrix, buttonBox);

        // Ajouter l'image et les informations dans la carte
        produitCard.getChildren().addAll(imageView, infoBox);

        return produitCard;
    }

    // Méthode pour ajouter un produit au panier
  
    private void ajouterAuPanier(Produit produit) {
        // Vérifier si le produit est déjà dans le panier
        if (produitsDansLePanier.containsKey(produit)) {
            // Incrémenter la quantité
            produitsDansLePanier.put(produit, produitsDansLePanier.get(produit) + 1);
        } else {
            // Ajouter le produit avec une quantité de 1
            produitsDansLePanier.put(produit, 1);
        }
    
        // Mettre à jour le compteur du panier
        cartCount++;
        cartCounter.setText(String.valueOf(cartCount));

    }

    // Méthode pour modifier un produit
    private void modifierProduit(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditProduitWindow.fxml"));
            Scene scene = new Scene(loader.load());
            EditProduitController editController = loader.getController();
            editController.setProduit(produit);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Modifier Produit");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour supprimer un produit
    private void supprimerProduit(Produit produit) throws Exception {
        Fabrique.getService().deleteProduit(produit);
        produits.remove(produit); // Mettre à jour la liste des produits
        displayPage(currentPage); // Rafraîchir l'affichage
    }

    // Méthode pour ouvrir un formulaire d'ajout de produit
    @FXML
    private void ajouterProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddProduitWindow.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Ajouter Produit");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showCart() {
        try {
            // Charger le fichier FXML du panier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/CartPane.fxml"));
            Parent cartPane = loader.load();
    
            // Obtenir le contrôleur du panier
            CartController cartController = loader.getController();
    
            // Passer les produits du panier
            cartController.setProduitsDansLePanier(produitsDansLePanier);
    
            // Calculer et afficher le prix total
            BigDecimal prixTotal = calculerPrixTotal();
            cartController.setPrixTotal(prixTotal);
    
            // Créer une nouvelle scène pour le panier
            Scene cartScene = new Scene(cartPane);
    
            // Créer une nouvelle fenêtre (Stage) pour le panier
            Stage cartStage = new Stage();
            cartStage.initModality(Modality.APPLICATION_MODAL); // Empêcher l'interaction avec la fenêtre principale
            cartStage.initStyle(StageStyle.UTILITY); // Style de fenêtre simple
            cartStage.setTitle("Panier");
            cartStage.setScene(cartScene);
    
            // Afficher la fenêtre du panier
            cartStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private BigDecimal calculerPrixTotal() {
    BigDecimal prixTotal = BigDecimal.ZERO; // Initialiser le prix total à 0

    for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
        Produit produit = entry.getKey();
        int quantite = entry.getValue();

        // Convertir la quantité en BigDecimal
        BigDecimal quantiteBigDecimal = new BigDecimal(quantite);

        // Multiplier le prix du produit par la quantité
        BigDecimal prixProduit = produit.getPrix().multiply(quantiteBigDecimal);

        // Ajouter au prix total
        prixTotal = prixTotal.add(prixProduit);
    }

    return prixTotal;
}

    // Méthodes pour la pagination
    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            displayPage(currentPage);
        }
    }

    @FXML
    private void nextPage() {
        int maxPages = (int) Math.ceil((double) produits.size() / (COLUMNS_PER_PAGE * ROWS_PER_COLUMN));
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
        }
    }
}