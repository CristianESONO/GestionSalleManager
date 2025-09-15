package com.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Client;
import com.entities.Produit;
import com.entities.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CartController {

    @FXML
    private ListView<String> cartItemsList;

    @FXML
    private Text prixTotalText;

    private Map<Produit, Integer> produitsDansLePanier;

    private ProduitController produitController;
    private User connectedUser;

    public void setProduitsDansLePanier(Map<Produit, Integer> produitsDansLePanier) {
        this.produitsDansLePanier = produitsDansLePanier;
        afficherElementsDuPanier();
        updateTotalDisplay();
    }

    public void setProduitController(ProduitController produitController) {
        this.produitController = produitController;
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }


    @FXML
    public void initialize() {
        // Menu contextuel pour modification/suppression via clic droit
        ContextMenu contextMenu = new ContextMenu();
        MenuItem modifyQuantityItem = new MenuItem("Modifier la quantité");
        modifyQuantityItem.setOnAction(event -> {
            String selectedItemText = cartItemsList.getSelectionModel().getSelectedItem();
            if (selectedItemText != null) {
                // Utiliser la méthode de modification qui demande la nouvelle quantité
                modifyProductQuantityWithDialog(selectedItemText);
            }
        });
        MenuItem deleteItem = new MenuItem("Supprimer");
        deleteItem.setOnAction(event -> {
            String selectedItemText = cartItemsList.getSelectionModel().getSelectedItem();
            if (selectedItemText != null) {
                removeProductFromCart(getProduitFromSelectedItemText(selectedItemText));
            }
        });
        contextMenu.getItems().addAll(modifyQuantityItem, deleteItem);
        cartItemsList.setContextMenu(contextMenu);

        // Cell Factory pour personnaliser l'affichage de chaque cellule avec des boutons
        cartItemsList.setCellFactory(lv -> new CartProductCell());
    }

    private void afficherElementsDuPanier() {
        cartItemsList.getItems().clear();
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            String itemText = String.format("%s x%d - %.2f FCFA", produit.getNom(), quantite, produit.getPrix().doubleValue() * quantite);
            cartItemsList.getItems().add(itemText);
        }
        updateTotalDisplay();
        updateCartCounter();
    }

    // Helper pour obtenir l'objet Produit à partir du texte de l'élément
    private Produit getProduitFromSelectedItemText(String itemText) {
        if (itemText == null || itemText.isEmpty()) {
            return null;
        }
        String productName;
        int indexOfX = itemText.indexOf(" x");
        if (indexOfX != -1) {
            productName = itemText.substring(0, indexOfX);
        } else {
            showAlert("Erreur de formatage", "Impossible d'extraire le nom du produit de : " + itemText);
            return null;
        }

        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            if (entry.getKey().getNom().equals(productName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Méthode pour modifier la quantité via un dialogue (utilisée par le menu contextuel)
    private void modifyProductQuantityWithDialog(String selectedItemText) {
        Produit selectedProduit = getProduitFromSelectedItemText(selectedItemText);
        if (selectedProduit == null) {
            showAlert("Erreur", "Produit introuvable dans le panier.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(produitsDansLePanier.get(selectedProduit)));
        dialog.setTitle("Modifier la quantité");
        dialog.setHeaderText("Modifier la quantité pour " + selectedProduit.getNom());
        dialog.setContentText("Nouvelle quantité :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int newQuantity = Integer.parseInt(qtyStr);
                modifyProductQuantity(selectedProduit, newQuantity - produitsDansLePanier.getOrDefault(selectedProduit, 0));
            } catch (NumberFormatException e) {
                showAlert("Erreur de saisie", "Veuillez entrer un nombre valide pour la quantité.");
            }
        });
    }

    // Méthode principale pour modifier la quantité (+/-)
    private void modifyProductQuantity(Produit produit, int delta) {
        if (produit == null) return;

        int currentQuantity = produitsDansLePanier.getOrDefault(produit, 0);
        int newQuantity = currentQuantity + delta;

        Produit actualProduitInDb = Fabrique.getService().findProduitById(produit.getId());
        if (actualProduitInDb == null) {
            showAlert("Erreur", "Produit introuvable dans la base de données.");
            return;
        }
        int stockDisponible = actualProduitInDb.getStock();

        if (newQuantity <= 0) {
            removeProductFromCart(produit);
        } else if (newQuantity > stockDisponible) {
            showAlert("Stock insuffisant", "Quantité demandée (" + newQuantity + ") dépasse le stock disponible (" + stockDisponible + ") pour " + produit.getNom());
        } else {
            produitsDansLePanier.put(produit, newQuantity);
            afficherElementsDuPanier();
        }
    }

    // Supprimer un produit du panier
    private void removeProductFromCart(Produit produit) {
        if (produit == null) {
            showAlert("Erreur", "Impossible de supprimer le produit : produit non spécifié.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer " + produit.getNom() + " du panier ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            produitsDansLePanier.remove(produit);
            afficherElementsDuPanier();
        }
    }

    @FXML
    private void closeCart() {
        Stage stage = (Stage) cartItemsList.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void viderPanier() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment vider tout le panier ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            produitsDansLePanier.clear();
            cartItemsList.getItems().clear();
            prixTotalText.setText("0 FCFA");
            updateCartCounter();
        }
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        String prixTotalFormate = String.format("%.2f FCFA", prixTotal);
        prixTotalText.setText(prixTotalFormate);
    }

    private void updateTotalDisplay() {
        setPrixTotal(BigDecimal.valueOf(calculerPrixTotal()));
    }

    // Mettre à jour le compteur global du panier (affiché dans ProduitController)
    private void updateCartCounter() {
        int totalItemsInCart = produitsDansLePanier.values().stream().mapToInt(Integer::intValue).sum();
        if (produitController != null && ProduitController.cartCounterGlobal != null) {
            ProduitController.cartCounterGlobal.setText(String.valueOf(totalItemsInCart));
        }
    }

    @FXML
    private void handlePayer() {
        if (produitsDansLePanier.isEmpty()) {
            showAlert("Panier vide", "Votre panier est vide. Veuillez ajouter des produits avant de procéder au paiement.");
            return;
        }
        // Vérifier que l'utilisateur est bien connecté
        if (connectedUser == null) {
            connectedUser = Fabrique.getService().getCurrentUser();
            if (connectedUser == null) {
                showAlert("Erreur", "Aucun utilisateur connecté détecté.");
                return;
            }
        }
        
        try {
            WindowManager.closeWindowsForView("PaymentWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/PaymentWindow.fxml"));
            Parent paymentPane = loader.load();

            PaymentController paymentController = loader.getController();
            List<Client> clients = Fabrique.getService().getAllClients();
            double montantTotal = calculerPrixTotal();
            String detailsProduits = getDetailsProduits();
            paymentController.setConnectedUser(connectedUser); // À ajouter
            paymentController.initializeData(clients, montantTotal, detailsProduits, "", produitsDansLePanier);

            
            // Callback pour le paiement réussi : vider le panier, fermer la fenêtre du panier et rafraîchir la liste des produits
            paymentController.setOnPaymentSuccessCallback(() -> {
                viderPanier();
                closeCart();
                if (produitController != null) {
                    produitController.refreshProduitList();
                }
            });

            Scene paymentScene = new Scene(paymentPane);
            Stage paymentStage = new Stage();
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.setTitle("Paiement");
            WindowManager.register("PaymentWindow", paymentStage);
            paymentStage.setScene(paymentScene);
            paymentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de l'ouverture de la fenêtre de paiement : " + e.getMessage());
        }
    }

    private double calculerPrixTotal() {
        double prixTotal = 0;
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            prixTotal += produit.getPrix().doubleValue() * quantite;
        }
        return prixTotal;
    }

    private String getDetailsProduits() {
        StringBuilder details = new StringBuilder();
        for (Map.Entry<Produit, Integer> entry : produitsDansLePanier.entrySet()) {
            Produit produit = entry.getKey();
            int quantite = entry.getValue();
            details.append(produit.getNom()).append(" x").append(quantite).append(", ");
        }
        if (details.length() > 0) {
            details.setLength(details.length() - 2);
        }
        return details.toString();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe interne pour les cellules personnalisées de la ListView, affichant des boutons +/- et X
    private class CartProductCell extends ListCell<String> {
        private HBox hbox = new HBox();
        private Text itemLabel = new Text();
        private Pane spacer = new Pane();
        private Button addButton = new Button("+");
        private Button minusButton = new Button("-");
        private Button removeButton = new Button("X");

        public CartProductCell() {
            super();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.setSpacing(5);
            hbox.getChildren().addAll(itemLabel, spacer, minusButton, addButton, removeButton);

            addButton.setOnAction(event -> {
                String currentText = getItem();
                if (currentText != null) {
                    Produit p = getProduitFromSelectedItemText(currentText);
                    if (p != null) {
                        modifyProductQuantity(p, 1);
                    }
                }
            });

            minusButton.setOnAction(event -> {
                String currentText = getItem();
                if (currentText != null) {
                    Produit p = getProduitFromSelectedItemText(currentText);
                    if (p != null) {
                        modifyProductQuantity(p, -1);
                    }
                }
            });

            removeButton.setOnAction(event -> {
                String currentText = getItem();
                if (currentText != null) {
                    Produit p = getProduitFromSelectedItemText(currentText);
                    if (p != null) {
                        removeProductFromCart(p);
                    }
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                itemLabel.setText(item);
                setGraphic(hbox);
            }
        }
    }
}