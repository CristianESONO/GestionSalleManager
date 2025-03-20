package com.controllers;

import com.core.Fabrique;
import com.entities.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;

public class GameController {

    @FXML
    private GridPane gridJeux; // Remplacez VBox par GridPane

    private List<Game> jeux; // Liste des jeux
    private int currentPage = 0; // Page actuelle
    private final int COLUMNS_PER_PAGE = 4; // Nombre de colonnes par page
    private final int ROWS_PER_COLUMN = 3; // Nombre de lignes par colonne

    @FXML
    public void initialize() {
        // Chargement des jeux au démarrage
        jeux = Fabrique.getService().getAllGames();
        displayPage(currentPage); // Afficher la première page
    }

    // Afficher une page spécifique
    private void displayPage(int page) {
        gridJeux.getChildren().clear(); // Effacer les cartes actuelles

        int startIndex = page * COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int endIndex = Math.min(startIndex + (COLUMNS_PER_PAGE * ROWS_PER_COLUMN), jeux.size());

        int row = 0;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Game game = jeux.get(i);

            // Créer une carte pour le jeu
            HBox gameCard = createGameCard(game);

            // Ajouter la carte au GridPane
            gridJeux.add(gameCard, col, row);

            // Passer à la ligne suivante après 3 cartes
            row++;
            if (row >= ROWS_PER_COLUMN) {
                row = 0;
                col++;
            }
        }
    }

    // Créer une carte pour un jeu
    private HBox createGameCard(Game game) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-padding: 10;");

        // Image du jeu
        ImageView gameImageView = new ImageView(game.getImagePath()); // Assurez-vous que chaque jeu a une image
        gameImageView.setFitHeight(100);
        gameImageView.setFitWidth(100);

        // Nom du jeu
        Label gameNameLabel = new Label(game.getName());

        // Type du jeu
        Label gameTypeLabel = new Label(game.getType());

        // Bouton Modifier (jaune)
        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnModifier.setOnAction(e -> openEditGameWindow(game));

        // Bouton Supprimer (rouge)
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnSupprimer.setOnAction(e -> supprimerJeu(game));

        // Ajouter les éléments à la carte
        VBox vbox = new VBox(10, gameNameLabel, gameTypeLabel, btnModifier, btnSupprimer);
        card.getChildren().addAll(gameImageView, vbox);

        return card;
    }

    // Passer à la page précédente
    @FXML
    private void previousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayPage(currentPage);
        }
    }

    // Passer à la page suivante
    @FXML
    private void nextPage(ActionEvent event) {
        int maxPages = (int) Math.ceil((double) jeux.size() / (COLUMNS_PER_PAGE * ROWS_PER_COLUMN));
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
        }
    }

    // Ouvrir la fenêtre pour modifier un jeu
    private void openEditGameWindow(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditGameWindow.fxml"));
            Scene scene = new Scene(loader.load());
            EditGameController editGameController = loader.getController();
            editGameController.setGame(game);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Modifier le Jeu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Supprimer un jeu
    private void supprimerJeu(Game game) {
        try {
            Fabrique.getService().deleteGame(game);
            jeux.remove(game); // Mettre à jour la liste des jeux
            displayPage(currentPage); // Rafraîchir l'affichage
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Ouvrir la fenêtre pour ajouter un jeu
    @FXML
    private void openAddGameWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddGameWindow.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Ajouter un Jeu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}