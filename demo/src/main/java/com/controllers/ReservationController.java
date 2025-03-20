package com.controllers;

import com.entities.Client;
import com.entities.Game;
import com.entities.Reservation;
import com.core.Fabrique;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationController {

    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, String> numeroTicketColumn;
    @FXML
    private TableColumn<Reservation, String> clientColumn;
    @FXML
    private TableColumn<Reservation, String> posteColumn;
    @FXML
    private TableColumn<Reservation, String> gameColumn; // Nouvelle colonne pour le jeu
    @FXML
    private TableColumn<Reservation, Integer> durationColumn;
    @FXML
    private TableColumn<Reservation, String> reservationDateColumn;
    @FXML
    private TableColumn<Reservation, String> totalPriceColumn; // Nouvelle colonne pour le prix total

    @FXML
    private TextField searchReservationField;

    private ObservableList<Reservation> reservations;

    @FXML
    public void initialize() {
        setupReservationTable();
        loadReservations();

        // Événement pour la recherche
        searchReservationField.textProperty().addListener((observable, oldValue, newValue) -> filterReservations(newValue));
    }

    private void setupReservationTable() {
        // Configurer les colonnes
        numeroTicketColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumeroTicket()));
        clientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClient().getName()));
        posteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPoste().getName()));
        gameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGame().getName())); // Configurer la colonne du jeu
        reservationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReservationDate().toString()));
        durationColumn.setCellValueFactory(cellData -> {
            Duration duration = cellData.getValue().getDuration();
            long minutes = duration.toMinutes();
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            int totalDurationInMinutes = (int) (hours * 60 + remainingMinutes);
            return new SimpleIntegerProperty(totalDurationInMinutes).asObject();
        });
          // Configurer la nouvelle colonne pour le prix total
        totalPriceColumn.setCellValueFactory(cellData -> {
            double totalPrice = cellData.getValue().calculateTotalPrice();
            return new SimpleStringProperty(String.format("%.2f FCFA", totalPrice)); // Formater le prix avec 2 décimales
        });
    }

    private void loadReservations() {
        List<Reservation> reservationList = Fabrique.getService().findAllReservations();
        reservations = FXCollections.observableArrayList(reservationList);
        reservationTable.setItems(reservations);
    }

    // Fonction de recherche pour filtrer les réservations
    private void filterReservations(String query) {
        if (query == null || query.isEmpty()) {
            reservationTable.setItems(reservations);
        } else {
            ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList(
                reservations.stream()
                    .filter(reservation -> {
                        Client client = reservation.getClient();
                        Game game = reservation.getGame();
                        double totalPrice = reservation.calculateTotalPrice();
                        boolean matchesClient = client != null && client.getName().toLowerCase().contains(query.toLowerCase());
                        boolean matchesTicket = reservation.getNumeroTicket().toLowerCase().contains(query.toLowerCase());
                        boolean matchesGame = game != null && game.getName().toLowerCase().contains(query.toLowerCase());
                        boolean matchesPrice = String.format("%.2f FCFA", totalPrice).toLowerCase().contains(query.toLowerCase());

                        return matchesClient || matchesTicket || matchesGame || matchesPrice;
                    })
                    .collect(Collectors.toList())
            );
            reservationTable.setItems(filteredReservations);
        }
    }

    @FXML
    private void openAddReservationWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/AddReservationWindow.fxml"));
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
    private void openEditReservationWindow() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une réservation à modifier.");
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/EditReservationWindow.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            EditReservationController controller = loader.getController();
            controller.loadReservationData(selectedReservation);
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
    private void deleteReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une réservation à supprimer.");
            alert.show();
            return;
        }

        // Logique de suppression de la réservation
        boolean success = Fabrique.getService().deleteGameSession(selectedReservation.getId());
        if (success) {
            loadReservations();  // Rafraîchir la table
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Réservation supprimée avec succès.");
            alert.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression de la réservation.");
            alert.show();
        }
    }
}