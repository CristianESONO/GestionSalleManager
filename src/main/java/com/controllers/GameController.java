package com.controllers;

import com.core.Fabrique;
import com.core.WindowManager;
import com.entities.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.control.ScrollPane; // Ajout de l'import pour ScrollPane

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameController {

    @FXML
    private GridPane gridJeux; // Le GridPane qui contient les cartes des jeux

    @FXML
    private ScrollPane scrollPaneJeux; // Le ScrollPane qui englobe le GridPane

    // Les boutons de pagination, si vous avez besoin de les désactiver/activer dynamiquement
    @FXML
    private Button btnPrecedent;
    @FXML
    private Button btnSuivant;

    private List<Game> jeux;
    private int currentPage = 0;
    private final int COLUMNS_PER_PAGE = 4;
    private final int ROWS_PER_COLUMN = 3; // Nombre de lignes par colonne avant de passer à la colonne suivante

    @FXML
    public void initialize() {
        // Ces propriétés peuvent maintenant être définies via CSS dans main-theme.css (grid-container)
        // gridJeux.setHgap(10);
        // gridJeux.setVgap(10);

        // Assurez-vous que le ScrollPane s'adapte au contenu du GridPane
        if (scrollPaneJeux != null) {
            scrollPaneJeux.setFitToWidth(true);
            // scrollPaneJeux.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Si vous ne voulez jamais de barre horizontale
        }

        // Chargement initial des jeux
        refreshGames();
    }

    /**
     * Rafraîchit la liste des jeux et met à jour l'affichage de la page actuelle.
     * Cette méthode est publique car elle sera appelée depuis AddGameController et EditGameController.
     */
    public void refreshGames() {
        jeux = Fabrique.getService().getAllGames();
        // Ajuster la page actuelle si elle dépasse le nombre total de pages après une mise à jour (ex: suppression)
        int totalItemsPerPage = COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int maxPages = (int) Math.ceil((double) jeux.size() / totalItemsPerPage);

        if (maxPages == 0) { // S'il n'y a plus de jeux du tout
            currentPage = 0;
        } else if (currentPage >= maxPages) { // Si la page actuelle n'existe plus (ex: après suppression de la dernière page)
            currentPage = maxPages - 1; // Revenir à la dernière page valide
        }
        displayPage(currentPage);
        updatePaginationButtons(); // Mettre à jour l'état des boutons de pagination
    }

    // Affiche une page spécifique de jeux
    private void displayPage(int page) {
        gridJeux.getChildren().clear(); // Effacer les cartes actuelles

        int totalItemsPerPage = COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int startIndex = page * totalItemsPerPage;
        int endIndex = Math.min(startIndex + totalItemsPerPage, jeux.size());

        int row = 0;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Game game = jeux.get(i);

            // Créer une carte pour le jeu
            VBox gameCard = createGameCard(game); // Changé HBox en VBox si c'est la structure de votre carte

            // Ajouter la carte au GridPane
            gridJeux.add(gameCard, col, row);

            // Gérer le positionnement des cartes dans la grille (4 colonnes, 3 lignes par colonne)
            row++;
            if (row >= ROWS_PER_COLUMN) {
                row = 0;
                col++;
            }
        }
    }

    // Mettre à jour l'état (activé/désactivé) des boutons de pagination
    private void updatePaginationButtons() {
        int totalItemsPerPage = COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int maxPages = (int) Math.ceil((double) jeux.size() / totalItemsPerPage);

        btnPrecedent.setDisable(currentPage == 0);
        btnSuivant.setDisable(currentPage >= (maxPages - 1) || maxPages == 0);
    }

    // Crée une carte visuelle pour un jeu
    private VBox createGameCard(Game game) { // Changé HBox en VBox pour la racine de la carte
        VBox card = new VBox(10); // Espacement vertical à l'intérieur de la carte
        card.getStyleClass().add("game-card-pane"); // Application de la classe CSS pour la carte

        // Image du jeu réduite
        ImageView gameImageView = new ImageView();
        try {
            File file = new File(game.getImagePath());
            if (file.exists() && !file.isDirectory()) {
                Image image = new Image(file.toURI().toString());
                gameImageView.setImage(image);
            } else {
                gameImageView.setImage(new Image(getClass().getResourceAsStream("/com/img/maxresdefault.jpg")));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image pour le jeu " + game.getName() + ": " + e.getMessage());
            gameImageView.setImage(new Image(getClass().getResourceAsStream("/com/img/maxresdefault.jpg")));
        }
        gameImageView.setFitHeight(90); // Taille augmentée pour l'image
        gameImageView.setFitWidth(90);  // Taille augmentée pour l'image
        gameImageView.setPreserveRatio(true);

        Label gameNameLabel = new Label(game.getName());
        gameNameLabel.getStyleClass().add("game-card-title"); // Application de la classe CSS

        Label gameTypeLabel = new Label(game.getType());
        gameTypeLabel.getStyleClass().add("game-card-label"); // Application de la classe CSS

        // Conteneur pour les boutons d'action
        HBox actionButtons = new HBox(8); // Espacement horizontal entre les boutons
        actionButtons.getStyleClass().add("card-action-buttons"); // Nouvelle classe CSS pour le conteneur des boutons

        // Bouton Modifier avec icône
        Button btnModifier = new Button();
        ImageView updateIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/img/update.png")));
        updateIcon.setFitHeight(20);
        updateIcon.setFitWidth(20);
        btnModifier.setGraphic(updateIcon);
        btnModifier.getStyleClass().add("icon-button-edit"); // Classe CSS spécifique pour Modifier
        btnModifier.setOnAction(e -> openEditGameWindow(game));
        btnModifier.setTooltip(new javafx.scene.control.Tooltip("Modifier le jeu")); // Info-bulle

        // Bouton Supprimer avec icône
        Button btnSupprimer = new Button();
        ImageView trashIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/img/trash.png")));
        trashIcon.setFitHeight(20);
        trashIcon.setFitWidth(20);
        btnSupprimer.setGraphic(trashIcon);
        btnSupprimer.getStyleClass().add("icon-button-delete"); // Classe CSS spécifique pour Supprimer
        btnSupprimer.setOnAction(e -> supprimerJeu(game));
        btnSupprimer.setTooltip(new javafx.scene.control.Tooltip("Supprimer le jeu")); // Info-bulle

        actionButtons.getChildren().addAll(btnModifier, btnSupprimer);

        // Ajout des éléments à la carte (VBox)
        card.getChildren().addAll(gameImageView, gameNameLabel, gameTypeLabel, actionButtons);

        return card;
    }

    // Passer à la page précédente
    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayPage(currentPage);
            updatePaginationButtons();
        }
    }

    // Passer à la page suivante
    @FXML
    private void nextPage(ActionEvent event) {
        int totalItemsPerPage = COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int maxPages = (int) Math.ceil((double) jeux.size() / totalItemsPerPage);
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
            updatePaginationButtons();
        }
    }

    // Ouvrir la fenêtre pour modifier un jeu
    private void openEditGameWindow(Game game) {
        try {
            WindowManager.closeWindowsForView("EditGameWindow");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditGameWindow.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            EditGameController editGameController = loader.getController();
            editGameController.setGame(game);
            editGameController.setGameController(this); // Passe une référence de ce contrôleur

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le Jeu");
            WindowManager.register("EditGameWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            error.setTitle("Erreur de chargement");
            error.setHeaderText(null);
            error.setContentText("Impossible d'ouvrir la fenêtre de modification du jeu.");
            error.showAndWait();
        }
    }

    // Supprimer un jeu
    private void supprimerJeu(Game game) {
        javafx.scene.control.Alert confirmation = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer le jeu : \"" + game.getName() + "\" ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    Fabrique.getService().deleteGame(game);
                    refreshGames(); // Rafraîchit l'affichage après la suppression

                    javafx.scene.control.Alert success = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    success.setTitle("Suppression réussie");
                    success.setHeaderText(null);
                    success.setContentText("Le jeu a été supprimé avec succès.");
                    success.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Une erreur est survenue lors de la suppression du jeu: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }

    // Ouvrir la fenêtre pour ajouter un jeu
    @FXML
    private void openAddGameWindow() {
        try {
            WindowManager.closeWindowsForView("AddGameWindow");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddGameWindow.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/css/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un Jeu");
            WindowManager.register("AddGameWindow", stage);
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            AddGameController addGameController = loader.getController();
            addGameController.setGameController(this); // Passe une référence de ce contrôleur

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            error.setTitle("Erreur de chargement");
            error.setHeaderText(null);
            error.setContentText("Impossible d'ouvrir la fenêtre d'ajout de jeu.");
            error.showAndWait();
        }
    }
}