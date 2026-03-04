package com.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
// Importations liées au menu et déconnexion supprimées car gérées par MainSceneController
// import javafx.scene.control.Button;
// import com.core.Fabrique;
// import com.entities.Role;
// import com.entities.User;


public class AccueilController implements Initializable {

    // Supprimez tous les @FXML des boutons de menu et du label de rôle
    // @FXML private Label lblUserRole;
    // @FXML private Button btnPosteJeu;
    // @FXML private Button btnPostes;
    // @FXML private Button btnJeux;
    // @FXML private Button btnReservations;
    // @FXML private Button btnPromotions;
    // @FXML private Button btnDeconnexion;

    private final DropShadow shadow = new DropShadow();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // La logique de visibilité du menu et du rôle est maintenant dans MainSceneController
        // User currentUser = Fabrique.getService().getCurrentUser();
        // boolean isSuperAdmin = currentUser != null && currentUser.getRole() == Role.SuperAdmin;
        // String roleName = currentUser != null ? currentUser.getRole().name() : "Invité";
        // lblUserRole.setText("(" + roleName + ")");

        // Les contrôles de visibilité des boutons sont aussi dans MainSceneController
        // btnPosteJeu.setVisible(!isSuperAdmin);
        // ...
    }

    // Gardez si vous voulez des effets visuels sur des éléments spécifiques à votre page d'accueil
    @FXML
    private void handleMouseEntered(MouseEvent event) {
        if (event.getSource() instanceof javafx.scene.control.Button button) {
            shadow.setColor(javafx.scene.paint.Color.web("#9643A9"));
            button.setEffect(shadow);
        }
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        if (event.getSource() instanceof javafx.scene.control.Button button) {
            button.setEffect(null);
        }
    }

    // Supprimez toutes les méthodes handleLoadView... et handleDeconnexion,
    // car elles sont maintenant dans MainSceneController.
    // @FXML public void handleLoadViewUtilisateurs() { ... }
    // @FXML public void handleDeconnexion() { ... }
}