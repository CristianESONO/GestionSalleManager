package com.controllers;

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class AccueilController {
    
    @FXML
    private AnchorPane mainContent;
    
    @FXML
    private Button btnDeconnexion;
    
    private final DropShadow shadow = new DropShadow();

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            shadow.setColor(javafx.scene.paint.Color.web("#9643A9"));
            button.setEffect(shadow);
        }
    }
    
    @FXML
    private void handleMouseExited(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            button.setEffect(null);
        }
    }
    
    public void handleLoadViewUtilisateurs() throws IOException {
        loadView("listuser");
    }
    
    public void handleLoadViewJeux() throws IOException {
        loadView("jeux");
    }
    
    public void handleLoadViewPostes() throws IOException {
        loadView("postes");
    }
    
    public void handleLoadViewReservations() throws IOException {
        loadView("reservations");
    }
    
    public void handleLoadViewProduits() throws IOException {
        loadView("produits");
    }
    
    public void handleLoadViewFinances() throws IOException {
        loadView("finances");
    }
    
    public void handleLoadViewSession() throws IOException {
        loadView("GameSession");
    }
    
    public void handleDeconnexion() throws IOException {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");
        Optional<ButtonType> option = alert.showAndWait();
    
        if (option.isPresent() && option.get().equals(ButtonType.OK)) {
            // Récupérer la fenêtre actuelle (Stage)
            Stage currentStage = (Stage) btnDeconnexion.getScene().getWindow();
    
            // Fermer la fenêtre actuelle (cela fermera l'application)
            currentStage.close();
        }
    }
    

    private void loadView(String fxml) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("/com/views/" + fxml + ".fxml"));
            mainContent.getChildren().clear();
            mainContent.getChildren().add(root);
            
            // Ancrage pour que la vue occupe tout l'espace disponible
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher un message d'erreur à l'utilisateur
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la vue : " + fxml);
            alert.showAndWait();
        }
    }
}
