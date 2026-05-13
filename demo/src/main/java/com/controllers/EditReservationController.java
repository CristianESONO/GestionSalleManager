package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Poste;
import com.entities.Reservation;
import com.entities.Game;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.time.Duration;
import java.util.List;
import java.time.ZoneId; // Pour ZoneId.systemDefault()

public class EditReservationController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField loyaltyPointsField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private ComboBox<Game> gameComboBox;
    @FXML
    private ComboBox<Poste> posteComboBox;
    @FXML
    private ComboBox<Integer> durationComboBox;
    @FXML
    private TextField codeParrainageField;
    @FXML
    private Button editButton;
    @FXML
    private Button cancelButton;

    private Reservation reservationToEdit; // Référence à la réservation à éditer

    @FXML
    public void initialize() {
        // Initialiser les ComboBox pour la durée, les jeux et les postes
        setupDurationComboBox();
        loadGames();
        loadPostes();

        // Configurer les boutons
        editButton.setOnAction(event -> editReservation());
        cancelButton.setOnAction(event -> cancel());
    }

    private void setupDurationComboBox() {
        // Ajouter des valeurs multiples de 15 minutes (15, 30, 45, 60, etc.)
        for (int i = 15; i <= 180; i += 15) {
            durationComboBox.getItems().add(i);
        }
        durationComboBox.getSelectionModel().selectFirst(); // Sélectionner la première valeur par défaut
    }

    private void loadGames() {
        List<Game> games = Fabrique.getService().getAllGames();
        gameComboBox.getItems().addAll(games);
    }

    private void loadPostes() {
        List<Poste> postes = Fabrique.getService().getPostes();
        posteComboBox.getItems().addAll(postes);
    }

    // Méthode pour charger les données de la réservation à éditer
    public void loadReservationData(Reservation reservation) {
        reservationToEdit = reservation;

        // Pré-remplir les champs client (en lecture seule)
        Client client = reservation.getClient();
        nameField.setText(client.getName());
        emailField.setText(client.getEmail());
        phoneField.setText(client.getPhone());
        addressField.setText(client.getAddress());
        loyaltyPointsField.setText(String.valueOf(client.getLoyaltyPoints()));
        roleComboBox.setValue(client.getRole().name());

        // Pré-remplir les champs de réservation
        gameComboBox.setValue(reservation.getGame());
        posteComboBox.setValue(reservation.getPoste());
        durationComboBox.setValue((int) reservation.getDuration().toMinutes());
        codeParrainageField.setText(reservation.getCodeParrainage());
    }

    // Méthode pour enregistrer les modifications de la réservation
    private void editReservation() {
        try {
            // Récupérer les valeurs modifiables
            Game selectedGame = gameComboBox.getValue();
            Poste selectedPoste = posteComboBox.getValue();
            int durationInMinutes = durationComboBox.getValue();
            String codeParrainage = codeParrainageField.getText();

            // Valider les champs obligatoires
            if (selectedGame == null || selectedPoste == null || durationInMinutes <= 0) {
                showError("Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            // Mettre à jour la réservation
            reservationToEdit.setGame(selectedGame);
            reservationToEdit.setPoste(selectedPoste);
            reservationToEdit.setDuration(Duration.ofMinutes(durationInMinutes));
            reservationToEdit.setCodeParrainage(codeParrainage);

            // Enregistrer les modifications dans la base de données
            Fabrique.getService().updateReservation(reservationToEdit);

            // Afficher un message de succès et fermer la fenêtre
            showInfo("Succès", "La réservation a été modifiée avec succès.");
            closeWindow();
        } catch (Exception e) {
            showError("Erreur", "Une erreur s'est produite lors de la modification de la réservation.");
        }
    }

    // Méthode pour annuler et fermer la fenêtre
    private void cancel() {
        closeWindow();
    }

    // Méthode pour fermer la fenêtre
    private void closeWindow() {
        ((AnchorPane) cancelButton.getScene().getRoot()).getScene().getWindow().hide();
    }

    // Méthode pour afficher un message d'erreur
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour afficher un message d'information
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}