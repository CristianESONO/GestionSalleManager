package com.controllers;

import com.entities.Client;
import com.entities.Game;
import com.entities.GameSession;
import com.core.Fabrique;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddGameSessionController {

    @FXML
    private ComboBox<Client> clientComboBox;
    @FXML
    private ComboBox<Game> gameComboBox;
    @FXML
    private TextField paidDurationField;
    @FXML
    private ComboBox<String> statusComboBox;

    private ObservableList<Client> clientList = FXCollections.observableArrayList();
    private ObservableList<Game> gameList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Charger la liste des clients et des jeux depuis le service ou la base de données
        loadClients();
        loadGames();
        
        // Initialiser le ComboBox des statuts
        statusComboBox.getItems().addAll("Pending", "Active", "Completed", "Stopped");
    }

    // Charger la liste des clients depuis un service ou base de données
    private void loadClients() {
        // Récupérer la liste des clients via le service (en utilisant Fabrique ici)
        clientList = FXCollections.observableArrayList(Fabrique.getService().getAllClients());
        clientComboBox.setItems(clientList);
    }

    // Charger la liste des jeux depuis un service ou base de données
    private void loadGames() {
        // Récupérer la liste des jeux via le service (en utilisant Fabrique ici)
        gameList = FXCollections.observableArrayList(Fabrique.getService().getAllGames());
        gameComboBox.setItems(gameList);
    }

    @FXML
    private void addGameSession() {
        try {
            // Récupérer les valeurs des champs
            Client selectedClient = clientComboBox.getValue();
            Game selectedGame = gameComboBox.getValue();
            long paidDuration = Long.parseLong(paidDurationField.getText());
            String status = statusComboBox.getValue();
    
            // Vérifier si un client et un jeu sont sélectionnés
            if (selectedClient == null || selectedGame == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez sélectionner un client et un jeu.");
                alert.show();
                return;
            }
    
            // Créer un nouvel objet GameSession
            GameSession newGameSession = new GameSession();
            newGameSession.setClient(selectedClient);
            newGameSession.setGame(selectedGame);
            newGameSession.setPaidDuration(java.time.Duration.ofMinutes(paidDuration));
            newGameSession.setStatus(status);
    
            // Enregistrer la session dans la base de données (en utilisant un service)
            GameSession gameSession = Fabrique.getService().addGameSession(newGameSession);
            if (gameSession != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Session de jeu ajoutée avec succès.");
                alert.show();
    
                // Fermer la fenêtre
                closeWindow();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout de la session.");
                alert.show();
            }
        } catch (NumberFormatException e) {
            // Gérer les erreurs de conversion des valeurs
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez entrer des valeurs valides.");
            alert.show();
        } catch (Exception e) {
            // Gérer les autres exceptions (par exemple erreurs lors de l'ajout de la session)
            Alert alert = new Alert(Alert.AlertType.ERROR, "Une erreur inattendue est survenue : " + e.getMessage());
            alert.show();
        }
    }
    
    @FXML
    private void cancel() {
        closeWindow();
    }

    // Méthode pour fermer la fenêtre
    private void closeWindow() {
        Stage stage = (Stage) clientComboBox.getScene().getWindow();
        stage.close();
    }
}
