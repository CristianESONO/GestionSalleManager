package com.controllers;

import com.entities.GameSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SessionEndDialogController {

    private GameSession session;

    @FXML
    private Label messageLabel;

    public void setSession(GameSession session) {
        this.session = session;
    }

    @FXML
    private void handleContinue() {
        // Ajoute 10 minutes par exemple
        session.addExtraTime(java.time.Duration.ofMinutes(10));
        session.setStatus("Active");
        close();
    }

    @FXML
    private void handleTerminate() {
        session.setStatus("Terminé");
        close();
    }

    private void close() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}
