package com.controllers;

import com.core.Fabrique;
import com.entities.Poste;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;

public class PosteController {

    @FXML
    private GridPane gridPostes; // Remplacez VBox par GridPane

    private List<Poste> postes; // Liste des postes
    private int currentPage = 0; // Page actuelle
    private final int COLUMNS_PER_PAGE = 6; // Nombre de colonnes par page
    private final int ROWS_PER_COLUMN = 4; // Nombre de lignes par colonne

    @FXML
    public void initialize() {
        // Chargement des postes au démarrage
        postes = Fabrique.getService().getPostes();
        displayPage(currentPage); // Afficher la première page
    }

    // Afficher une page spécifique
    private void displayPage(int page) {
        gridPostes.getChildren().clear(); // Effacer les cartes actuelles

        int startIndex = page * COLUMNS_PER_PAGE * ROWS_PER_COLUMN;
        int endIndex = Math.min(startIndex + (COLUMNS_PER_PAGE * ROWS_PER_COLUMN), postes.size());

        int row = 0;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Poste poste = postes.get(i);

            // Créer une carte pour le poste
            StackPane posteCard = createPosteCard(poste);

            // Ajouter la carte au GridPane
            gridPostes.add(posteCard, col, row);

            // Passer à la ligne suivante après 5 cartes
            row++;
            if (row >= ROWS_PER_COLUMN) {
                row = 0;
                col++;
            }
        }
    }

    // Créer une carte pour un poste
    private StackPane createPosteCard(Poste poste) {
        StackPane posteCard = new StackPane();
        posteCard.setMaxWidth(600);

        HBox hbox = new HBox(10);
        hbox.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-padding: 10;");

        // Partie colorée selon la disponibilité du poste
        Region statusColor = new Region();
        statusColor.setPrefWidth(50);
        statusColor.setStyle("-fx-background-color: " + (poste.isAvailable() ? "#000000" : "#808080") + "; -fx-border-radius: 10;");
        // Nom du poste et boutons
        VBox vbox = new VBox(10);
        Text posteName = new Text(poste.getName());
        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #B0BEC5; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-radius: 5; -fx-background-radius: 5;");


        // Actions des boutons
        btnModifier.setOnAction(event -> editPoste(poste));
        btnSupprimer.setOnAction(event -> {
            try {
                deletePoste(poste);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        vbox.getChildren().addAll(posteName, btnModifier, btnSupprimer);
        hbox.getChildren().addAll(statusColor, vbox);
        posteCard.getChildren().add(hbox);

        return posteCard;
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
        int maxPages = (int) Math.ceil((double) postes.size() / (COLUMNS_PER_PAGE * ROWS_PER_COLUMN));
        if (currentPage < maxPages - 1) {
            currentPage++;
            displayPage(currentPage);
        }
    }

    // Modifier un poste
    private void editPoste(Poste poste) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditPosteWindow.fxml"));
            Scene scene = new Scene(loader.load());
            EditPosteController editPosteController = loader.getController();
            editPosteController.setPoste(poste);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Modifier le Poste");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Supprimer un poste
    private void deletePoste(Poste poste) throws Exception {
        Fabrique.getService().deletePoste(poste);
        postes.remove(poste); // Mettre à jour la liste des postes
        displayPage(currentPage); // Rafraîchir l'affichage
    }

    // Ouvrir la fenêtre pour ajouter un poste
    @FXML
    private void openAddPosteWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddPosteWindow.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Ajouter un Poste");
        stage.show();
    }
}