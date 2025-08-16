package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Poste;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.paint.Color; // Import pour Color

import java.io.IOException;
import java.sql.SQLException; // Import pour gérer SQLException
import java.util.List;
import java.util.Optional;

public class PosteController {

    @FXML
    private GridPane gridPostes;

    // Déclarez les boutons de pagination et d'ajout s'ils ont des fx:id dans votre FXML
    @FXML private Button btnAjouterPoste;
    @FXML private Button btnPrecedent; // Ajouté pour correspondre au FXML
    @FXML private Button btnSuivant;   // Ajouté pour correspondre au FXML

    private List<Poste> postes;
    private int currentPage = 0;
    private final int COLUMNS_PER_PAGE = 4;
    private final int ROWS_PER_COLUMN = 2; // Cela signifie 8 postes par page (4x2)

    @FXML
    public void initialize() {
        gridPostes.setHgap(20);
        gridPostes.setVgap(20);
        loadPostes(); // Charger les postes au démarrage
    }

    /**
     * Charge les postes depuis le service et rafraîchit l'affichage.
     * Cette méthode est publique pour être appelée par d'autres contrôleurs (ex: AddPosteController)
     * après une modification de données.
     */
    public void loadPostes() {
        try {
            postes = Fabrique.getService().getPostes(); // Re-charger la liste complète des postes
            System.out.println("DEBUG: Nombre de postes chargés depuis le service: " + postes.size()); // DEBUG
            displayPage(currentPage);
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible de charger les postes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayPage(int page) {
        gridPostes.getChildren().clear(); // Nettoyer la grille avant d'afficher la nouvelle page

        int startIndex = page * COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int endIndex = Math.min(startIndex + (COLUMNS_PER_PAGE * ROWS_PER_COLUMN), postes.size());

        System.out.println("DEBUG: Affichage de la page " + page + ". Index de début: " + startIndex + ", Index de fin: " + endIndex); // DEBUG

        int row = 0;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Poste poste = postes.get(i);
            System.out.println("DEBUG: Ajout du poste à la grille: " + poste.getName() + " (ID: " + poste.getId() + ") à la colonne " + col + ", ligne " + row); // DEBUG
            VBox posteCard = createPosteCard(poste);
            gridPostes.add(posteCard, col, row);

            col++;
            if (col >= COLUMNS_PER_PAGE) {
                col = 0;
                row++;
            }
        }
        if (startIndex >= endIndex) {
            System.out.println("DEBUG: Aucune carte de poste ajoutée pour cette page. La liste des postes est peut-être vide ou la page est en dehors des limites.");
        }
    }

    /**
     * Crée une carte de poste stylisée pour l'affichage.
     *
     * @param poste The Poste data to display.
     * @return A VBox styled as a poste card.
     */
    private VBox createPosteCard(Poste poste) {
        VBox cardContent = new VBox(10); // Espacement entre les éléments
        cardContent.setPadding(new Insets(15));
        cardContent.setAlignment(Pos.TOP_LEFT);
        cardContent.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-border-radius: 10px; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");

        cardContent.setMaxWidth(Double.MAX_VALUE);
        cardContent.setPrefHeight(Region.USE_COMPUTED_SIZE);

        String statutTexte;
        String statusColorHex;

        if (poste.isHorsService()) {
            statutTexte = "Hors service";
            statusColorHex = "#D50000"; // Rouge
        } else {
            statutTexte = "Disponible";
            statusColorHex = "#00C853"; // Vert
        }

        HBox statusIndicator = new HBox(5);
        statusIndicator.setAlignment(Pos.CENTER_LEFT);
        
        Region colorBar = new Region();
        colorBar.setPrefWidth(20);
        colorBar.setPrefHeight(20);
        colorBar.setStyle("-fx-background-color: " + statusColorHex + "; -fx-background-radius: 5px;");

        Label statusLabel = new Label(statutTexte);
        statusLabel.setFont(Font.font("Arial", 14));
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");

        statusIndicator.getChildren().addAll(colorBar, statusLabel);
        VBox.setMargin(statusIndicator, new Insets(0, 0, 10, 0));

        Label posteName = new Label(poste.getName());
        posteName.setFont(Font.font("Arial", 20));
        posteName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        VBox.setMargin(posteName, new Insets(0, 0, 10, 0));

        // Boutons d'action (Modifier, Supprimer)
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(actionButtons, new Insets(15, 0, 0, 0));

        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().add("card-button-modify");
        btnModifier.setOnAction(event -> editPoste(poste));
        btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13px; " +
                             "-fx-padding: 8px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("card-button-delete");
        btnSupprimer.setOnAction(event -> deletePoste(poste)); // Appel direct, la gestion d'erreur est dans la méthode
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; " +
                             "-fx-padding: 8px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");

        actionButtons.getChildren().addAll(btnModifier, btnSupprimer);

        cardContent.getChildren().addAll(statusIndicator, posteName, actionButtons);

        return cardContent;
    }

    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayPage(currentPage);
        }
    }

    @FXML
    private void nextPage(ActionEvent event) {
        int maxPages = (int) Math.ceil((double) postes.size() / (COLUMNS_PER_PAGE * ROWS_PER_COLUMN));
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
        }
    }

    private void editPoste(Poste poste) {
        try {
            WindowManager.closeWindowsForView("EditPosteWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditPosteWindow.fxml"));
            Scene scene = new Scene(loader.load());
            EditPosteController editPosteController = loader.getController();
            editPosteController.setPoste(poste);
            // Passer une référence à ce contrôleur pour rafraîchir après modification
            editPosteController.setParentController(this); 

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le Poste");
            WindowManager.register("EditPosteWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait(); // Utiliser showAndWait() pour bloquer le parent et rafraîchir après fermeture
            
            // Rafraîchir la liste après la fermeture de la fenêtre de modification
            loadPostes(); 

        } catch (IOException e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible d'ouvrir la fenêtre de modification du poste : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deletePoste(Poste poste) { // Supprime 'throws Exception'
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer le poste : \"" + poste.getName() + "\" ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Fabrique.getService().deletePoste(poste);
                ControllerUtils.showInfoAlert("Suppression réussie", "Le poste a été supprimé avec succès.");
                loadPostes(); // Recharger la liste après suppression pour mettre à jour l'affichage

            } catch (SQLException e) {
                // Gérer spécifiquement l'erreur SQLite BUSY ou d'autres erreurs SQL
                if (e.getMessage() != null && e.getMessage().contains("database is locked")) {
                    ControllerUtils.showErrorAlert("Erreur de base de données",
                        "La base de données est actuellement verrouillée. Veuillez réessayer. Si le problème persiste, redémarrez l'application.");
                } else {
                    ControllerUtils.showErrorAlert("Erreur lors de la suppression",
                        "Une erreur SQL s'est produite lors de la suppression du poste : " + e.getMessage());
                }
                e.printStackTrace();
            } catch (Exception e) {
                ControllerUtils.showErrorAlert("Erreur inattendue", "Une erreur inattendue s'est produite lors de la suppression du poste : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openAddPosteWindow() { // Supprime 'throws IOException'
        try {
            WindowManager.closeWindowsForView("AddPosteWindow");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddPosteWindow.fxml"));
            Scene scene = new Scene(loader.load());
            AddPosteController addPosteController = loader.getController();
            // Passer une référence à ce contrôleur pour rafraîchir après ajout
            addPosteController.setParentController(this); 

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un Poste");
            WindowManager.register("AddPosteWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait(); // Utiliser showAndWait() pour bloquer le parent et rafraîchir après fermeture
            
            // Rafraîchir la liste après la fermeture de la fenêtre d'ajout
            loadPostes(); 

        } catch (IOException e) {
            ControllerUtils.showErrorAlert("Erreur de chargement", "Impossible d'ouvrir la fenêtre d'ajout de poste : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
