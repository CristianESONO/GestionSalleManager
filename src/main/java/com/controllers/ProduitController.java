package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Produit;
import com.entities.Role;
import com.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane; // Pour le badge PROMO
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text; // Pour le compteur du panier
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle; // Pour le style de la fenêtre du panier

import java.io.File; // Import pour gérer les fichiers locaux
import java.io.IOException;
import java.net.MalformedURLException; // Import pour gérer les URL mal formées
import java.sql.SQLException;
import java.math.BigDecimal; // Pour les prix
import java.time.LocalDate; // Pour les dates de péremption
import java.time.format.DateTimeFormatter; // Pour formater les dates
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProduitController {

    @FXML
    private GridPane gridProduits;

    @FXML
    private Button btnAjouterProduit;

    @FXML
    private ImageView cartIcon; // Icône du panier

    @FXML
    private Text cartCounter; // Compteur d'articles dans le panier

    public static Text cartCounterGlobal; // Référence statique pour le compteur du panier

    @FXML private Button btnPrecedent; // Bouton pour la pagination
    @FXML private Button btnSuivant;   // Bouton pour la pagination

    private List<Produit> produits;
    private int currentPage = 0;
    // Ajustez ces valeurs pour un affichage optimal avec la nouvelle taille de carte
    private final int COLUMNS_PER_PAGE = 4;
    private final int ROWS_PER_COLUMN = 2;

    private Map<Produit, Integer> produitsDansLePanier = new HashMap<>();

    // Rôles de l'utilisateur pour le contrôle de l'interface utilisateur
    private boolean isSuperAdmin;
    private boolean isAdmin;

    // Constantes pour les dimensions des images des produits
    private static final int PRODUCT_IMAGE_WIDTH = 120; // Largeur fixe pour les images
    private static final int PRODUCT_IMAGE_HEIGHT = 120; // Hauteur fixe pour les images

    @FXML
    public void initialize() {
        // Initialisation des propriétés du GridPane
        gridProduits.setHgap(20);
        gridProduits.setVgap(20);
        gridProduits.setPadding(new Insets(10)); // Marge autour du GridPane

        // Assigner la référence statique pour le compteur du panier
        cartCounterGlobal = cartCounter;

        User currentUser = Fabrique.getService().getCurrentUser();

        // Déterminer les rôles de l'utilisateur
        // Un Admin a accès aux fonctionnalités d'Admin (modifier/supprimer)
        // Un SuperAdmin a accès aux fonctionnalités de SuperAdmin (ajouter) et d'Admin
        isAdmin = currentUser.getRole() == Role.Admin || currentUser.getRole() == Role.SuperAdmin;
        isSuperAdmin = currentUser.getRole() == Role.SuperAdmin;

        // Gérer la visibilité du bouton "Ajouter Produit" en fonction du rôle SuperAdmin
        if (btnAjouterProduit != null) {
            btnAjouterProduit.setVisible(isSuperAdmin);
            btnAjouterProduit.setManaged(isSuperAdmin);
            
            // Appliquer le style au bouton Ajouter Produit si visible
            if (isSuperAdmin) {
                btnAjouterProduit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 20px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-cursor: hand;");
                DropShadow ds = new DropShadow();
                ds.setColor(Color.rgb(0, 0, 0, 0.2));
                ds.setOffsetX(0);
                ds.setOffsetY(2);
                ds.setRadius(5);
                btnAjouterProduit.setEffect(ds);
            }
        }

        // Cacher les éléments du panier pour les utilisateurs SuperAdmin
        if (isSuperAdmin) {
            if (cartIcon != null) {
                cartIcon.setVisible(false);
                cartIcon.setManaged(false);
            }
            if (cartCounter != null) {
                cartCounter.setVisible(false);
                cartCounter.setManaged(false);
            }
        }

        refreshProduitList(); // Charger les produits au démarrage
        checkExpiredProducts(); // Vérifier les produits expirés
        updateCartCounterDisplay(); // Mettre à jour l'affichage du compteur du panier au démarrage
    }

    /**
     * Charge les produits depuis le service et rafraîchit l'affichage.
     * Cette méthode est publique pour être appelée par d'autres contrôleurs (ex: AddProduitController)
     * après une modification de données.
     */
    public void refreshProduitList() {
        try {
            produits = Fabrique.getService().findAllProduits(); // Re-charger la liste complète des produits
            System.out.println("DEBUG: Nombre de produits chargés depuis le service: " + produits.size()); // DEBUG
            // Vérifier les produits expirés à chaque rafraîchissement
            checkExpiredProducts();
            displayPage(currentPage);
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible de charger les produits : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour l'affichage du compteur du panier.
     */
    public void updateCartCounterDisplay() {
        // Mettre à jour le compteur du panier uniquement si ce n'est pas un SuperAdmin et que le compteur existe
        if (!isSuperAdmin && cartCounterGlobal != null) {
            int totalItemsInCart = produitsDansLePanier.values().stream().mapToInt(Integer::intValue).sum();
            cartCounterGlobal.setText(String.valueOf(totalItemsInCart));
        }
    }

    /**
     * Affiche les produits pour le numéro de page donné dans le GridPane.
     *
     * @param page Le numéro de page à afficher.
     */
    private void displayPage(int page) {
        gridProduits.getChildren().clear(); // Effacer les cartes existantes

        int startIndex = page * COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int endIndex = Math.min(startIndex + (COLUMNS_PER_PAGE * ROWS_PER_COLUMN), produits.size());

        System.out.println("DEBUG: Affichage de la page " + page + ". Index de début: " + startIndex + ", Index de fin: " + endIndex); // DEBUG

        int row = 0;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Produit produit = produits.get(i);
            System.out.println("DEBUG: Ajout du produit à la grille: " + produit.getNom() + " (ID: " + produit.getId() + ") à la colonne " + col + ", ligne " + row); // DEBUG
            VBox produitCardContainer = createProduitCard(produit);
            gridProduits.add(produitCardContainer, col, row);

            col++; // Passer à la colonne suivante
            if (col >= COLUMNS_PER_PAGE) { // Si la limite de colonnes est atteinte, passer à la ligne suivante
                col = 0;
                row++;
            }
        }
        if (startIndex >= endIndex && produits.isEmpty()) { // Condition ajustée
            System.out.println("DEBUG: Aucune carte de produit ajoutée pour cette page. La liste des produits est vide.");
        } else if (startIndex >= endIndex && !produits.isEmpty()) {
            System.out.println("DEBUG: Aucune carte de produit ajoutée pour cette page. La page est en dehors des limites pour les produits restants.");
        }
    }

    /**
     * Crée une VBox stylisée et moderne représentant une seule carte de produit.
     * Inclut le nom, le prix, le stock, l'image et les boutons d'administration/ajout au panier.
     *
     * @param produit Les données du produit à afficher.
     * @return Une VBox stylisée comme une carte de produit.
     */
    private VBox createProduitCard(Produit produit) {
        // Style du conteneur principal de la carte
        VBox produitCard = new VBox(10); // Espacement entre les éléments à l'intérieur de la carte
        produitCard.setPrefWidth(220); // Largeur fixe ajustée pour des cartes cohérentes
        produitCard.setPrefHeight(280); // Hauteur fixe ajustée
        produitCard.setAlignment(Pos.TOP_CENTER); // Centrer le contenu horizontalement
        produitCard.setPadding(new Insets(15)); // Rembourrage intérieur
        produitCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-border-radius: 10px;");

        // Ajout d'un effet d'ombre portée pour la profondeur
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1)); // Ombre noire douce
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(3);
        produitCard.setEffect(dropShadow);

        // Mettre en évidence les produits périmés avec une bordure rouge
        if (produit.getDateLimiteConsommation() != null &&
            !produit.getDateLimiteConsommation().isAfter(LocalDate.now())) {
            produitCard.setStyle(produitCard.getStyle() + "-fx-border-color: #e74c3c; -fx-border-width: 3px;"); // Bordure rouge vibrante
        }

        // Configuration de l'image du produit
        ImageView imageView = new ImageView();
        try {
            String imagePath = produit.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    // CHARGEMENT CORRECT DE L'IMAGE À PARTIR D'UN CHEMIN ABSOLU
                    Image image = new Image(file.toURI().toURL().toExternalForm(), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT, false, true); // Taille fixe
                    imageView.setImage(image);
                } else {
                    System.err.println("Failed to load image for product " + produit.getNom() + ": File does not exist at " + imagePath + ". Using placeholder.");
                    // Fallback vers une image de remplacement si le fichier n'existe pas, avec taille fixe
                    imageView.setImage(new Image(getClass().getResourceAsStream("/com/img/placeholder.png"), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT, false, true));
                }
            } else {
                System.out.println("No image path provided for product " + produit.getNom() + ". Using placeholder.");
                imageView.setImage(new Image(getClass().getResourceAsStream("/com/img/placeholder.png"), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT, false, true));
            }
        } catch (MalformedURLException e) {
            System.err.println("Failed to load image for product " + produit.getNom() + ": Invalid URL for " + produit.getImage() + ". Using placeholder. Error: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/img/placeholder.png"), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT, false, true));
        } catch (Exception e) {
            System.err.println("Failed to load image for product " + produit.getNom() + ": Error loading image. Using placeholder. Error: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/img/placeholder.png"), PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT, false, true));
        }
        // Assurez-vous que la taille est appliquée même si l'image est redimensionnée lors du chargement
        imageView.setFitHeight(PRODUCT_IMAGE_HEIGHT);
        imageView.setFitWidth(PRODUCT_IMAGE_WIDTH);
        imageView.setPreserveRatio(false); // Permet d'étirer l'image pour remplir les dimensions
        imageView.setCache(true); // Mettre l'image en cache pour la performance
        VBox.setMargin(imageView, new Insets(0, 0, 5, 0)); // Marge sous l'image

        // Nom du produit (utilisant Label pour un style cohérent)
        Label produitName = new Label(produit.getNom());
        produitName.setFont(Font.font("Arial", 16));
        produitName.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

        // Logique d'affichage de la promotion et du prix
        boolean isEnPromotion = produit.getAncienPrix() != null &&
                                 produit.getPrix().compareTo(produit.getAncienPrix()) < 0;

        VBox priceContainer = new VBox(2); // Conteneur pour les éléments de prix
        priceContainer.setAlignment(Pos.CENTER);

        if (isEnPromotion) {
            // Badge "PROMO"
            Label promoLabel = new Label("PROMO");
            promoLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 11px; " +
                                 "-fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-font-weight: bold;");
            StackPane promoBadge = new StackPane(promoLabel);
            promoBadge.setAlignment(Pos.CENTER);
            VBox.setMargin(promoBadge, new Insets(0, 0, 5, 0));

            // Ancien prix (barré)
            Label produitPrixAncien = new Label(produit.getAncienPrix().toPlainString() + " FCFA");
            produitPrixAncien.setFont(Font.font("Arial", 12));
            produitPrixAncien.setStyle("-fx-text-fill: #95a5a6; -fx-strikethrough: true;");

            // Prix actuel (promotionnel)
            Label produitPrixActuel = new Label(produit.getPrix().toPlainString() + " FCFA");
            produitPrixActuel.setFont(Font.font("Arial", 15));
            produitPrixActuel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            priceContainer.getChildren().addAll(promoBadge, produitPrixAncien, produitPrixActuel);
        } else {
            // Affichage du prix normal
            Label produitPrixActuel = new Label("Prix : " + produit.getPrix().toPlainString() + " FCFA");
            produitPrixActuel.setFont(Font.font("Arial", 14));
            produitPrixActuel.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");
            priceContainer.getChildren().add(produitPrixActuel);
        }

        // Informations sur le stock
        Label produitStock = new Label("Stock : " + produit.getStock());
        produitStock.setFont(Font.font("Arial", 12));
        produitStock.setStyle("-fx-text-fill: #7f8c8d;");

        // Informations sur la date limite de consommation (DLC)
        Label produitDateLimite = new Label();
        if (produit.getDateLimiteConsommation() != null) {
            produitDateLimite.setText("DLC : " + produit.getDateLimiteConsommation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            produitDateLimite.setText("DLC : Non définie");
        }
        produitDateLimite.setFont(Font.font("Arial", 11));
        produitDateLimite.setStyle("-fx-text-fill: #7f8c8d;");

        // Alerte d'expiration
        Label alerteExpiration = null;
        if (produit.getDateLimiteConsommation() != null && !produit.getDateLimiteConsommation().isAfter(LocalDate.now())) {
            alerteExpiration = new Label("⚠ Expired");
            alerteExpiration.setFont(Font.font("Arial", 11));
            alerteExpiration.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }

        // Conteneur des boutons d'action
        HBox buttonBox = new HBox(8); // Espacement entre les boutons
        buttonBox.setAlignment(Pos.CENTER); // Centrer les boutons
        VBox.setMargin(buttonBox, new Insets(10, 0, 0, 0)); // Marge au-dessus des boutons

        // Bouton "Ajouter au Panier" (visible uniquement pour les non-SuperAdmin)
        if (!isSuperAdmin) {
            Button btnAjouter = new Button("➕ Ajouter"); // Icône d'ajout
            btnAjouter.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; " +
                                 "-fx-padding: 6px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
            // Ajout d'une ombre portée au bouton
            DropShadow btnShadow = new DropShadow();
            btnShadow.setColor(Color.rgb(0, 0, 0, 0.2));
            btnShadow.setOffsetX(0);
            btnShadow.setOffsetY(2);
            btnShadow.setRadius(3);
            btnAjouter.setEffect(btnShadow);
            
            btnAjouter.setOnAction(event -> ajouterAuPanier(produit));
            buttonBox.getChildren().add(btnAjouter);
        }

        // Boutons "Modifier" et "Supprimer" (visibles pour Admin ou SuperAdmin)
        if (isSuperAdmin) {
            Button btnModifier = new Button("⚙ Modifier"); // Icône d'engrenage
            btnModifier.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 11px; " +
                                 "-fx-padding: 6px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
            // Ajout d'une ombre portée au bouton
            DropShadow btnShadow = new DropShadow();
            btnShadow.setColor(Color.rgb(0, 0, 0, 0.2));
            btnShadow.setOffsetX(0);
            btnShadow.setOffsetY(2);
            btnShadow.setRadius(3);
            btnModifier.setEffect(btnShadow);

            btnModifier.setOnAction(event -> modifierProduit(produit));

            Button btnSupprimer = new Button("🗑 Supprimer"); // Icône de poubelle
            btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; " +
                                 "-fx-padding: 6px 12px; -fx-background-radius: 5px; -fx-cursor: hand;");
            // Ajout d'une ombre portée au bouton
            btnSupprimer.setEffect(btnShadow); // Réutiliser la même ombre pour la cohérence

            btnSupprimer.setOnAction(event -> supprimerProduit(produit)); // Appel direct, la gestion d'erreur est dans la méthode
            buttonBox.getChildren().addAll(btnModifier, btnSupprimer);
        }

        // Ajouter tous les éléments construits à la carte du produit
        produitCard.getChildren().addAll(imageView, produitName, priceContainer, produitStock, produitDateLimite);
        if (alerteExpiration != null) {
            produitCard.getChildren().add(alerteExpiration);
        }
        produitCard.getChildren().add(buttonBox);

        return produitCard;
    }

    /**
     * Ajoute un produit au panier, avec des vérifications de stock et de péremption.
     * @param produit Le produit à ajouter.
     */
    private void ajouterAuPanier(Produit produit) {
        if (produit.getDateLimiteConsommation() != null &&
            !produit.getDateLimiteConsommation().isAfter(LocalDate.now())) {
            ControllerUtils.showInfoAlert("Produit expiré", "Ce produit est périmé et ne peut pas être ajouté au panier.");
            return;
        }

        int currentQuantityInCart = produitsDansLePanier.getOrDefault(produit, 0);
        if (produit.getStock() > currentQuantityInCart) {
            produitsDansLePanier.put(produit, currentQuantityInCart + 1);
            updateCartCounterDisplay();
            ControllerUtils.showInfoAlert("Ajout au panier", "Le produit '" + produit.getNom() + "' a été ajouté au panier.");
        } else {
            ControllerUtils.showInfoAlert("Stock insuffisant", "Il n'y a plus de stock disponible pour ce produit.");
        }
    }

    /**
     * Ouvre une fenêtre pour modifier un produit existant.
     * @param produit Le produit à modifier.
     */
    private void modifierProduit(Produit produit) {
        try {
            WindowManager.closeWindowsForView("EditProduitWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditProduitWindow.fxml"));
            Scene scene = new Scene(loader.load());
            EditProduitController editController = loader.getController();
            editController.setProduit(produit);
            // Utilisation d'un callback pour rafraîchir la liste après modification
            editController.setParentController(this); // Passer une référence à ce contrôleur
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Produit");
            WindowManager.register("EditProduitWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait(); // Utiliser showAndWait() pour bloquer le parent et rafraîchir après fermeture
            
            refreshProduitList(); // Rafraîchir la liste après la fermeture de la fenêtre de modification
        } catch (IOException e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible d'ouvrir la fenêtre de modification du produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkExpiredProducts() 
    {
        List<Produit> expiredProducts = produits.stream()
                .filter(p -> p.getDateLimiteConsommation() != null 
                        && !p.getDateLimiteConsommation().isAfter(LocalDate.now())
                        && p.getStock() > 0)
                .collect(Collectors.toList());

        if (!expiredProducts.isEmpty()) {
            // Créer une alerte
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Produits expirés");
            alert.setHeaderText(expiredProducts.size() + " produit(s) expiré(s) détecté(s)");
            
            // Créer le contenu de l'alerte
            StringBuilder content = new StringBuilder("Les produits suivants sont expirés et leur stock a été remis à 0 :\n\n");
            for (Produit p : expiredProducts) {
                content.append("- ").append(p.getNom())
                    .append(" (DLC: ").append(p.getDateLimiteConsommation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .append(")\n");
            }
            alert.setContentText(content.toString());
            
            // Afficher l'alerte
            alert.showAndWait();
            
            // Mettre à jour le stock des produits expirés
            for (Produit p : expiredProducts) {
                try {
                    p.setStock(0); // Remise à zéro du stock
                    Fabrique.getService().updateProduit(p); // Sauvegarde en base
                } catch (Exception e) {
                    ControllerUtils.showErrorAlert("Erreur de mise à jour", 
                        "Erreur lors de la mise à jour du produit " + p.getNom() + ": " + e.getMessage());
                }
            }
            
            // Rafraîchir la liste des produits
            refreshProduitList();
        }
    }

    /**
     * Gère la suppression d'un produit après confirmation de l'utilisateur.
     * @param produit Le produit à supprimer.
     */
    private void supprimerProduit(Produit produit) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer ce produit : \"" + produit.getNom() + "\" ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Fabrique.getService().deleteProduit(produit);
                refreshProduitList();
                ControllerUtils.showInfoAlert("Suppression réussie", "Le produit a été supprimé avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                ControllerUtils.showErrorAlert("Erreur de suppression", "Une erreur est survenue lors de la suppression du produit : " + e.getMessage());
            }
        }
    }

    /**
     * Ouvre une fenêtre pour ajouter un nouveau produit.
     */
    @FXML
    private void ajouterProduit() { // Renommé de openAddProduitWindow pour correspondre à votre FXML
        try {
            WindowManager.closeWindowsForView("AddProduitWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddProduitWindow.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter Produit");
            WindowManager.register("AddProduitWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            AddProduitController addController = loader.getController();
            // Passer une référence à ce contrôleur pour rafraîchir après ajout
            addController.setParentController(this); // Utilise la méthode setParentController pour rafraîchir

            stage.showAndWait(); // Utiliser showAndWait() pour bloquer le parent et rafraîchir après fermeture
            refreshProduitList(); // Rafraîchir la liste après la fermeture de la fenêtre d'ajout
        } catch (IOException e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible d'ouvrir la fenêtre d'ajout de produit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche la fenêtre du panier d'achat.
     */
    @FXML
    private void showCart() {
        // Empêcher les SuperAdmins d'accéder au panier
        if (isSuperAdmin) {
            ControllerUtils.showInfoAlert("Accès refusé", "Les SuperAdmins n'ont pas accès au panier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/CartPane.fxml"));
            Parent cartPane = loader.load();

            CartController cartController = loader.getController();
            cartController.setProduitsDansLePanier(produitsDansLePanier);
            cartController.setProduitController(this); // Passer la référence de ce contrôleur

            BigDecimal prixTotal = calculerPrixTotal();
            cartController.setPrixTotal(prixTotal);

            Stage cartStage = new Stage();
            cartStage.initModality(Modality.APPLICATION_MODAL);
            cartStage.initStyle(StageStyle.UTILITY);
            cartStage.setTitle("Panier");
            cartStage.setScene(new Scene(cartPane));
            WindowManager.register("CartPane", cartStage);
            cartStage.sizeToScene();
            cartStage.setResizable(false);
            cartStage.centerOnScreen();

            cartStage.show();
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", "Impossible d'ouvrir le panier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calcule le prix total de tous les articles actuellement dans le panier.
     * @return Le prix total sous forme de BigDecimal.
     */
    private BigDecimal calculerPrixTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit p = entry.getKey();
            int qte = entry.getValue();
            total = total.add(p.getPrix().multiply(new BigDecimal(qte)));
        }
        return total;
    }

    /**
     * Navigue vers la page précédente des produits.
     */
    @FXML
    private void previousPage(ActionEvent event) { // Garder ActionEvent pour la cohérence
        if (currentPage > 0) {
            currentPage--;
            displayPage(currentPage);
        }
    }

    /**
     * Navigue vers la page suivante des produits.
     */
    @FXML
    private void nextPage(ActionEvent event) { // Garder ActionEvent pour la cohérence
        int maxPages = (int) Math.ceil((double) produits.size() / (COLUMNS_PER_PAGE * ROWS_PER_COLUMN));
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
        }
    }
}
