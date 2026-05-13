package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Categorie;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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
import java.io.InputStream; // Ajoutez cette ligne
import java.net.MalformedURLException; // Import pour gérer les URL mal formées
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @FXML private TextField searchProduitField;
    @FXML private ComboBox<Categorie> categoryFilterComboBox;
    @FXML private Button btnGererCategories;

    private List<Produit> produits;
    private List<Produit> allProduits;
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

        // v1.3.5 : bouton Catégories (super admin uniquement)
        if (btnGererCategories != null) {
            btnGererCategories.setVisible(isSuperAdmin);
            btnGererCategories.setManaged(isSuperAdmin);
        }
        // v1.3.5 : filtre par catégorie
        if (categoryFilterComboBox != null) {
            categoryFilterComboBox.getItems().clear();
            categoryFilterComboBox.getItems().add(null); // "Toutes"
            categoryFilterComboBox.getItems().addAll(Fabrique.getService().getAllCategories());
            categoryFilterComboBox.setConverter(new StringConverter<Categorie>() {
                @Override
                public String toString(Categorie c) {
                    return c == null ? "Toutes les catégories" : (c.getNom() != null ? c.getNom() : "");
                }
                @Override
                public Categorie fromString(String s) { return null; }
            });
            categoryFilterComboBox.valueProperty().addListener((obs, o, n) -> applyCategoryAndSearchFilter());
        }
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

        if (searchProduitField != null) {
            searchProduitField.textProperty().addListener((obs, oldVal, newVal) -> applyCategoryAndSearchFilter());
        }

        // v1.3.5 : pagination -> liste sans fin (on masque les boutons)
        if (btnPrecedent != null) { btnPrecedent.setVisible(false); btnPrecedent.setManaged(false); }
        if (btnSuivant != null) { btnSuivant.setVisible(false); btnSuivant.setManaged(false); }

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
            applyCategoryAndSearchFilter();
            checkExpiredProducts();
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible de charger les produits : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** v1.3.5 : applique filtre catégorie puis filtre recherche par nom. */
    private void applyCategoryAndSearchFilter() {
        try {
            Categorie selectedCat = categoryFilterComboBox != null ? categoryFilterComboBox.getValue() : null;
            Integer catId = selectedCat != null ? selectedCat.getId() : null;
            allProduits = Fabrique.getService().findProduitsByCategorieId(catId);
            String q = searchProduitField != null && searchProduitField.getText() != null ? searchProduitField.getText().trim().toLowerCase() : "";
            if (q.isEmpty()) {
                produits = allProduits != null ? allProduits : List.of();
            } else {
                produits = (allProduits != null ? allProduits : List.<Produit>of()).stream()
                    .filter(p -> (p.getNom() != null && p.getNom().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
            }
            displayPage(0);
        } catch (Exception e) {
            produits = List.of();
            displayPage(0);
        }
    }

    @FXML
    private void openCategoriesManager(ActionEvent event) {
        try {
            WindowManager.closeWindowsForView("CategoriesManagerWindow");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/views/CategoriesManagerWindow.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Gestion des catégories");
            WindowManager.register("CategoriesManagerWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
            if (categoryFilterComboBox != null) {
                Categorie prev = categoryFilterComboBox.getValue();
                categoryFilterComboBox.getItems().clear();
                categoryFilterComboBox.getItems().add(null);
                categoryFilterComboBox.getItems().addAll(Fabrique.getService().getAllCategories());
                categoryFilterComboBox.setValue(prev);
            }
            refreshProduitList();
        } catch (Exception e) {
            e.printStackTrace();
            ControllerUtils.showErrorAlert("Erreur", "Impossible d'ouvrir la gestion des catégories.");
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

        // v1.3.5 : afficher toute la liste (scroll "sans fin")
        int startIndex = 0;
        int endIndex = produits != null ? produits.size() : 0;

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
                // Essayer de charger l'image du produit
                File imageFile = resolveImageFile(imagePath);
                
                if (imageFile != null && imageFile.exists()) {
                    // CHARGEMENT DE L'IMAGE DU PRODUIT
                    Image image = new Image(imageFile.toURI().toURL().toExternalForm(), 
                                        PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT, false, true);
                    imageView.setImage(image);
                } else {
                    // Image produit non trouvée → utiliser un placeholder
                    createDynamicPlaceholder(imageView, produit.getNom());
                }
            } else {
                // Pas d'image spécifiée → utiliser un placeholder
                createDynamicPlaceholder(imageView, produit.getNom());
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image pour " + produit.getNom() + ": " + e.getMessage());
            createDynamicPlaceholder(imageView, produit.getNom());
        }
        
        // Configuration de la taille
        imageView.setFitHeight(PRODUCT_IMAGE_HEIGHT);
        imageView.setFitWidth(PRODUCT_IMAGE_WIDTH);
        imageView.setPreserveRatio(true); // Garder les proportions
        imageView.setCache(true);
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

    // Méthode pour résoudre le fichier image
private File resolveImageFile(String imagePath) {
    try {
        // 1. Si c'est un chemin absolu Windows
        if (imagePath.contains(":\\")) {
            return new File(imagePath);
        }
        
        // 2. Si le chemin contient "src\main\resources" (anciens chemins de développement)
        if (imagePath.contains("src\\main\\resources") || imagePath.contains("src/main/resources")) {
            // Extraire juste le nom du fichier
            String fileName = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
            System.out.println("DEBUG - Nom de fichier extrait: " + fileName);
            
            // Chercher dans APPDATA/GestionSalles/produits/
            String appDataPath = System.getenv("APPDATA");
            if (appDataPath != null) {
                Path newPath = Paths.get(appDataPath, "GestionSalles", "produits", fileName);
                File file = newPath.toFile();
                System.out.println("DEBUG - Nouveau chemin: " + newPath + " existe: " + file.exists());
                return file;
            }
        }
        
        // 3. Chemin relatif vers APPDATA
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath != null) {
            Path absolutePath = Paths.get(appDataPath, "GestionSalles", imagePath);
            return absolutePath.toFile();
        }
        
        // 4. Dernier recours
        return new File(imagePath);
        
    } catch (Exception e) {
        System.err.println("Erreur dans resolveImageFile: " + e.getMessage());
        return null;
    }
}

// Méthode pour créer un placeholder dynamique avec le nom du produit
private void createDynamicPlaceholder(ImageView imageView, String productName) {
    try {
        // Créer une image simple
        WritableImage placeholder = new WritableImage(PRODUCT_IMAGE_WIDTH, PRODUCT_IMAGE_HEIGHT);
        PixelWriter pixelWriter = placeholder.getPixelWriter();
        
        // Couleur de fond basée sur le nom
        int hash = Math.abs(productName.hashCode());
        Color bgColor = Color.hsb(hash % 360, 0.2, 0.95);
        
        // Remplir l'arrière-plan
        for (int y = 0; y < PRODUCT_IMAGE_HEIGHT; y++) {
            for (int x = 0; x < PRODUCT_IMAGE_WIDTH; x++) {
                pixelWriter.setColor(x, y, bgColor);
            }
        }
        
        // Ajouter un simple cadre
        Color frameColor = Color.hsb((hash + 180) % 360, 0.5, 0.7);
        drawSimpleFrame(pixelWriter, frameColor);
        
        imageView.setImage(placeholder);
        
    } catch (Exception e) {
        // Fallback simple
        System.err.println("Erreur création placeholder: " + e.getMessage());
        imageView.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #d0d0d0; -fx-border-width: 1px;");
    }
}

private void drawSimpleFrame(PixelWriter pw, Color color) {
    int width = PRODUCT_IMAGE_WIDTH;
    int height = PRODUCT_IMAGE_HEIGHT;
    
    // Simple cadre de 2 pixels
    for (int x = 0; x < width; x++) {
        pw.setColor(x, 0, color); // Haut
        pw.setColor(x, 1, color);
        pw.setColor(x, height - 1, color); // Bas
        pw.setColor(x, height - 2, color);
    }
    
    for (int y = 0; y < height; y++) {
        pw.setColor(0, y, color); // Gauche
        pw.setColor(1, y, color);
        pw.setColor(width - 1, y, color); // Droite
        pw.setColor(width - 2, y, color);
    }
}



// Méthodes utilitaires de dessin...
private void drawCircle(PixelWriter pw, int centerX, int centerY, int radius, Color color) {
    for (int y = -radius; y <= radius; y++) {
        for (int x = -radius; x <= radius; x++) {
            if (x*x + y*y <= radius*radius) {
                int px = centerX + x;
                int py = centerY + y;
                if (px >= 0 && px < PRODUCT_IMAGE_WIDTH && py >= 0 && py < PRODUCT_IMAGE_HEIGHT) {
                    pw.setColor(px, py, color);
                }
            }
        }
    }
}

private void drawInitialPattern(PixelWriter pw, int centerX, int centerY, String initials, Color color) {
    // Dessiner un motif simple basé sur les initiales
    // (Par exemple, un point pour chaque lettre)
    if (initials.length() >= 1) {
        // Premier caractère : point à gauche
        drawPoint(pw, centerX - 8, centerY, color);
    }
    if (initials.length() >= 2) {
        // Deuxième caractère : point à droite
        drawPoint(pw, centerX + 8, centerY, color);
    }
}

private void drawPoint(PixelWriter pw, int x, int y, Color color) {
    for (int dy = -2; dy <= 2; dy++) {
        for (int dx = -2; dx <= 2; dx++) {
            int px = x + dx;
            int py = y + dy;
            if (px >= 0 && px < PRODUCT_IMAGE_WIDTH && py >= 0 && py < PRODUCT_IMAGE_HEIGHT) {
                pw.setColor(px, py, color);
            }
        }
    }
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

        // Recharger le produit depuis la base de données
        Produit produitMisAJour = Fabrique.getService().findProduitById(produit.getId());

        System.out.println("Prix avant promotion : " + produitMisAJour.getPrix());

        // Appliquer la promotion si elle est valide
        produitMisAJour.appliquerPromotion();

        System.out.println("Prix après promotion : " + produitMisAJour.getPrix());

        int currentQuantityInCart = produitsDansLePanier.getOrDefault(produitMisAJour, 0);
        if (produitMisAJour.getStock() > currentQuantityInCart) {
            produitsDansLePanier.put(produitMisAJour, currentQuantityInCart + 1);
            updateCartCounterDisplay();
            ControllerUtils.showInfoAlert("Ajout au panier", "Le produit '" + produitMisAJour.getNom() + "' a été ajouté au panier.");
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
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
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
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
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
            Scene cartScene = new Scene(cartPane);
            cartScene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            cartStage.setScene(cartScene);
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
