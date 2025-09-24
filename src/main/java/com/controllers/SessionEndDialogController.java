package com.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.core.Fabrique;
import com.entities.GameSession;
import com.entities.Reservation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class SessionEndDialogController {

    private GameSession session;
    private static ReservationController reservationController;
    private String connectedUserName;


    public static void setReservationController(ReservationController controller) {
        SessionEndDialogController.reservationController = controller;
    }

    public void setConnectedUserName(String userName) {
    this.connectedUserName = userName;
}


    @FXML
    private Label messageLabel;

    public void setSession(GameSession session) {
        this.session = session;
    }

   @FXML
private void handleContinue() {
    // Appeler une méthode similaire à promptForExtension()
    promptForExtension(session);
}

private void promptForExtension(GameSession session) {
    // Création du dialogue personnalisé
    Dialog<Pair<Integer, String>> dialog = new Dialog<>();
    dialog.setTitle("Prolonger la durée");
    dialog.setHeaderText("Prolonger la session pour le poste " + session.getPoste().getName());

    // Configuration des boutons
    ButtonType validerButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(validerButtonType, ButtonType.CANCEL);

    // Création du contenu du dialogue
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // ComboBox pour les heures (0 à 24)
    ComboBox<Integer> hoursComboBox = new ComboBox<>();
    for (int i = 0; i <= 24; i++) {
        hoursComboBox.getItems().add(i);
    }
    hoursComboBox.getSelectionModel().select(0); // Sélectionne 0 par défaut

    // ComboBox pour les minutes (15, 30, 45)
    ComboBox<Integer> minutesComboBox = new ComboBox<>();
    minutesComboBox.getItems().addAll(15, 30, 45);
    minutesComboBox.getSelectionModel().select(0); // Sélectionne 15 par défaut

    // ComboBox pour les moyens de paiement
    ComboBox<String> paymentMethodComboBox = new ComboBox<>();
    List<String> paymentMethods = getAvailablePaymentMethods();
    paymentMethodComboBox.getItems().addAll(paymentMethods);
    paymentMethodComboBox.getSelectionModel().selectFirst();

    // Ajout des éléments au grid
    grid.add(new Label("Heures:"), 0, 0);
    grid.add(hoursComboBox, 1, 0);
    grid.add(new Label("Minutes:"), 0, 1);
    grid.add(minutesComboBox, 1, 1);
    grid.add(new Label("Moyen de paiement:"), 0, 2);
    grid.add(paymentMethodComboBox, 1, 2);

    dialog.getDialogPane().setContent(grid);

    // Conversion du résultat
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == validerButtonType) {
            try {
                int hours = hoursComboBox.getSelectionModel().getSelectedItem();
                int minutes = minutesComboBox.getSelectionModel().getSelectedItem();
                int additionalMinutes = hours * 60 + minutes;

                if (additionalMinutes <= 0) {
                    ControllerUtils.showErrorAlert("Erreur de durée", "Veuillez sélectionner une durée valide.");
                    return null;
                }

                String selectedPaymentMethod = paymentMethodComboBox.getSelectionModel().getSelectedItem();
                if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
                    ControllerUtils.showErrorAlert("Erreur", "Veuillez sélectionner un moyen de paiement.");
                    return null;
                }

                return new Pair<>(additionalMinutes, selectedPaymentMethod);
            } catch (Exception e) {
                ControllerUtils.showErrorAlert("Erreur de saisie", "Veuillez sélectionner une durée et un moyen de paiement valides.");
                return null;
            }
        }
        return null;
    });

    // Affichage du dialogue
    Optional<Pair<Integer, String>> result = dialog.showAndWait();

    // Traitement du résultat
     result.ifPresent(pair -> {
        try {
            int additionalMinutes = pair.getKey();
            String selectedPaymentMethod = pair.getValue();
            session.addExtraTime(java.time.Duration.ofMinutes(additionalMinutes));
            session.setStatus("Active"); // Remettre à "Active"
            Fabrique.getService().extendGameSession(session, additionalMinutes, connectedUserName, selectedPaymentMethod);
            close();
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", "Échec de la prolongation : " + e.getMessage());
        }
    
});

}


private List<String> getAvailablePaymentMethods() {
    List<String> paymentMethods = new ArrayList<>();
    paymentMethods.add("En Espèce");
    paymentMethods.add("Wave");
    paymentMethods.add("Orange Money");
    paymentMethods.add("Free Money");
    paymentMethods.add("Wizall Money");
    paymentMethods.add("Carte Bancaire");
    return paymentMethods;
}


   @FXML
    private void handleTerminate() throws Exception {
        session.setStatus("Terminée");
        session.setEndTime(LocalDateTime.now());
        Reservation reservation = session.getReservation();
        if (reservation != null) {
            reservation.setStatus("Terminée");
            Fabrique.getService().updateReservation(reservation);
        }
        Fabrique.getService().updateGameSession(session);
        close();
    }



    private void close() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}
