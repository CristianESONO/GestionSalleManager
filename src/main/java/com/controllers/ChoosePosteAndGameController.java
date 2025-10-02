package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Game;
import com.entities.GameSession;
import com.entities.Poste;
import com.entities.Reservation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChoosePosteAndGameController {
    @FXML
    private ComboBox<Game> gameComboBox;
    @FXML
    private ComboBox<Poste> posteComboBox;

    private Client client;
    private Duration remainingTime;
    private UserController parentController;
    private List<Poste> allPostes;
    private List<Game> allGames;

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        loadAllGames();
        loadAllPostes();
        setupPosteGameFiltering();
    }

    private void setupComboBoxConverters() {
        // Converter pour Poste
        posteComboBox.setConverter(new StringConverter<Poste>() {
            @Override
            public String toString(Poste poste) {
                if (poste == null) return "";
                return "Poste " + poste.getId() + " - " + (poste.getName() != null ? poste.getName() : "");
            }
            @Override
            public Poste fromString(String string) {
                return null;
            }
        });

        // Converter pour Game
        gameComboBox.setConverter(new StringConverter<Game>() {
            @Override
            public String toString(Game game) {
                return game != null ? game.getName() : "";
            }
            @Override
            public Game fromString(String string) {
                return null;
            }
        });
    }

    private void loadAllGames() {
        try {
            allGames = Fabrique.getService().getAllGames();
            if (allGames == null) allGames = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            allGames = new ArrayList<>();
        }
    }

    private void loadAllPostes() {
        try {
            allPostes = Fabrique.getService().getPostes();
            if (allPostes == null) allPostes = new ArrayList<>();
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
        } catch (Exception e) {
            e.printStackTrace();
            allPostes = new ArrayList<>();
            posteComboBox.setItems(FXCollections.observableArrayList());
        }
    }

   private void setupPosteGameFiltering() {
    // Variables pour empêcher la récursion
    final boolean[] filteringPoste = {false};
    final boolean[] filteringGame = {false};

    // Quand on sélectionne un poste, filtrer les jeux disponibles sur ce poste
    posteComboBox.valueProperty().addListener((obs, oldVal, selectedPoste) -> {
        if (filteringGame[0]) return; // Ignorer si c'est le filtre de jeu qui a déclenché le changement
        filteringPoste[0] = true;
        filterGamesByPoste(selectedPoste);
        filteringPoste[0] = false;
    });

    // Quand on sélectionne un jeu, filtrer les postes compatibles avec ce jeu
    gameComboBox.valueProperty().addListener((obs, oldVal, selectedGame) -> {
        if (filteringPoste[0]) return; // Ignorer si c'est le filtre de poste qui a déclenché le changement
        filteringGame[0] = true;
        filterPostesByGame(selectedGame);
        filteringGame[0] = false;
    });
}

private void filterGamesByPoste(Poste selectedPoste) {
    try {
        // Sauvegarder la sélection actuelle du jeu
        Game currentGame = gameComboBox.getValue();
        
        if (selectedPoste == null) {
            // Si aucun poste sélectionné, afficher tous les jeux
            gameComboBox.setItems(FXCollections.observableArrayList(allGames));
        } else {
            // Filtrer les jeux disponibles sur le poste sélectionné
            List<Game> availableGames = allGames.stream()
                .filter(game -> selectedPoste.getGames() != null && selectedPoste.getGames().contains(game))
                .collect(Collectors.toList());
            gameComboBox.setItems(FXCollections.observableArrayList(availableGames));
        }
        
        // Restaurer la sélection si elle est toujours valide
        if (currentGame != null && gameComboBox.getItems().contains(currentGame)) {
            gameComboBox.setValue(currentGame);
        } else {
            gameComboBox.setValue(null);
        }
    } catch (Exception e) {
        e.printStackTrace();
        gameComboBox.setItems(FXCollections.observableArrayList(allGames));
    }
}

private void filterPostesByGame(Game selectedGame) {
    try {
        // Sauvegarder la sélection actuelle du poste
        Poste currentPoste = posteComboBox.getValue();
        
        if (selectedGame == null) {
            // Si aucun jeu sélectionné, afficher tous les postes
            posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
        } else {
            // Filtrer les postes qui ont le jeu sélectionné
            List<Poste> compatiblePostes = allPostes.stream()
                .filter(poste -> poste.getGames() != null && poste.getGames().contains(selectedGame))
                .collect(Collectors.toList());
            posteComboBox.setItems(FXCollections.observableArrayList(compatiblePostes));
        }
        
        // Restaurer la sélection si elle est toujours valide
        if (currentPoste != null && posteComboBox.getItems().contains(currentPoste)) {
            posteComboBox.setValue(currentPoste);
        } else {
            posteComboBox.setValue(null);
        }
    } catch (Exception e) {
        e.printStackTrace();
        posteComboBox.setItems(FXCollections.observableArrayList(allPostes));
    }
}

    public void setClient(Client client) {
        this.client = client;
    }

    public void setRemainingTime(Duration remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setParentController(UserController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void validate() {
        Game selectedGame = gameComboBox.getValue();
        Poste selectedPoste = posteComboBox.getValue();

        if (selectedGame == null || selectedPoste == null) {
            ControllerUtils.showErrorAlert("Erreur", "Veuillez sélectionner un jeu et un poste.");
            return;
        }

        try {
            // Utilisez directement le service pour reprendre la session
            Fabrique.getService().resumePausedSessionForClient(
                client.getId(), 
                selectedPoste.getId(), 
                selectedGame.getId()
            );

            // Fermer la fenêtre
            Stage stage = (Stage) gameComboBox.getScene().getWindow();
            stage.close();

            // Rafraîchir les données
            if (parentController != null) {
                parentController.refreshAllData();
                parentController.loadClientsWithRemainingTime();
            }

            ControllerUtils.showInfoAlert("Succès", "Session reprise avec succès sur le poste " + selectedPoste.getName());
        } catch (Exception e) {
            e.printStackTrace();
            ControllerUtils.showErrorAlert("Erreur", "Erreur lors de la reprise de la session: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) gameComboBox.getScene().getWindow();
        stage.close();
    }
}