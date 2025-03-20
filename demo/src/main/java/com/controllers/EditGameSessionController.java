package com.controllers;

import com.entities.GameSession;
import com.core.Fabrique;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditGameSessionController {

    @FXML
    private TextField clientField;
    @FXML
    private TextField gameField;
    @FXML
    private TextField paidDurationField;
    @FXML
    private ComboBox<String> statusComboBox;

    private GameSession gameSession;

    @FXML
    public void initialize() {
        // Initialiser la liste des statuts
        statusComboBox.getItems().addAll("Pending", "Active", "Completed", "Stopped");
    }

    // Méthode pour initialiser les données de la session de jeu
    public void initData(GameSession gameSession) {
        this.gameSession = gameSession;

        // Pré-remplir les champs avec les informations de la session de jeu existante
        clientField.setText(gameSession.getClient().getName());
        gameField.setText(gameSession.getGame().getName());
        paidDurationField.setText(String.valueOf(gameSession.getPaidDuration().toMinutes()));
        statusComboBox.setValue(gameSession.getStatus());
    }

    @FXML
    private void saveGameSession() {
        try {
            // Récupérer les valeurs des champs de saisie
            String clientName = clientField.getText();
            String gameName = gameField.getText();
            long paidDuration = Long.parseLong(paidDurationField.getText());
            String status = statusComboBox.getValue();

            // Mettre à jour les informations de la session de jeu
            gameSession.getClient().setName(clientName);
            gameSession.getGame().setName(gameName);
            gameSession.setPaidDuration(java.time.Duration.ofMinutes(paidDuration));
            gameSession.setStatus(status);

            // Sauvegarder les modifications (vous pouvez appeler un service pour cela)
            boolean success = Fabrique.getService().updateGameSession(gameSession);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Session de jeu modifiée avec succès.");
                alert.show();

                // Fermer la fenêtre modale
                closeWindow();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la modification de la session.");
                alert.show();
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez entrer des valeurs valides.");
            alert.show();
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    // Méthode pour fermer la fenêtre modale
    private void closeWindow() {
        Stage stage = (Stage) clientField.getScene().getWindow();
        stage.close();
    }
}
