package com.controllers;

import com.entities.GameSession;
import com.core.Fabrique;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GameSessionController {

    @FXML
    private TableView<GameSession> gameSessionTable;
    @FXML
    private TableColumn<GameSession, Integer> idColumn;
    @FXML
    private TableColumn<GameSession, String> clientColumn;
    @FXML
    private TableColumn<GameSession, String> gameColumn;
    @FXML
    private TableColumn<GameSession, Integer> paidDurationColumn;
    @FXML
    private TableColumn<GameSession, String> statusColumn;

    @FXML
    private TextField searchGameSessionField;

    private ObservableList<GameSession> gameSessions;

    @FXML
    public void initialize() {
        setupGameSessionTable();
        loadGameSessions();

        // Événement pour la recherche
        searchGameSessionField.textProperty().addListener((observable, oldValue, newValue) -> filterGameSessions(newValue));
    }

    private void setupGameSessionTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        clientColumn.setCellValueFactory(cellData -> {
            GameSession session = cellData.getValue();
            return new SimpleStringProperty(session.getClient() != null ? session.getClient().getName() : "N/A");
        });

        gameColumn.setCellValueFactory(cellData -> {
            GameSession session = cellData.getValue();
            return new SimpleStringProperty(session.getGame() != null ? session.getGame().getName() : "N/A");
        });

        paidDurationColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty((int) cellData.getValue().getPaidDuration().toMinutes()).asObject());

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private void loadGameSessions() {
        List<GameSession> sessions = Fabrique.getService().getAllGameSessions();
        gameSessions = FXCollections.observableArrayList(sessions);

        // Log les sessions avec des clients ou des jeux null
        gameSessions.forEach(session -> {
            if (session.getClient() == null) {
                System.out.println("Attention : La session de jeu avec l'ID " + session.getId() + " n'a pas de client.");
            }
            if (session.getGame() == null) {
                System.out.println("Attention : La session de jeu avec l'ID " + session.getId() + " n'a pas de jeu.");
            }
        });

        gameSessionTable.setItems(gameSessions);
    }

    private void filterGameSessions(String query) {
        if (query == null || query.isEmpty()) {
            gameSessionTable.setItems(gameSessions);
        } else {
            ObservableList<GameSession> filteredSessions = FXCollections.observableArrayList(
                gameSessions.stream()
                    .filter(session -> {
                        String clientName = session.getClient() != null ? session.getClient().getName() : "";
                        String gameName = session.getGame() != null ? session.getGame().getName() : "";
                        return clientName.toLowerCase().contains(query.toLowerCase()) ||
                               gameName.toLowerCase().contains(query.toLowerCase());
                    })
                    .collect(Collectors.toList())
            );
            gameSessionTable.setItems(filteredSessions);
        }
    }

    @FXML
    private void openAddGameSessionWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddGameSessionWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fenêtre d'ajout.");
            alert.show();
        }
    }

    @FXML
    private void openEditGameSessionWindow() {
        GameSession selectedSession = gameSessionTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une session à modifier.");
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditGameSessionWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            EditGameSessionController controller = loader.getController();
            controller.initData(selectedSession);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture de la fenêtre de modification.");
            alert.show();
        }
    }

    @FXML
    private void deleteGameSession() {
        GameSession selectedSession = gameSessionTable.getSelectionModel().getSelectedItem();
        if (selectedSession == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une session à supprimer.");
            alert.show();
            return;
        }

        try {
            // Logique de suppression de la session de jeu
            boolean success = Fabrique.getService().deleteGameSession(selectedSession.getId());
            if (success) {
                loadGameSessions();  // Rafraîchir la table
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Session supprimée avec succès.");
                alert.show();
            } else {
                // This 'else' block will catch cases where deleteGameSession returns false,
                // but not if it throws an exception (that's what the catch block is for).
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression de la session.");
                alert.show();
            }
        } catch (Exception e) {
            // Here, you catch the Exception thrown by deleteGameSession
            // You should log the exception for debugging purposes
            e.printStackTrace(); // This prints the stack trace to the console
            Alert alert = new Alert(Alert.AlertType.ERROR, "Une erreur inattendue est survenue lors de la suppression : " + e.getMessage());
            alert.show();
        }
    }
}