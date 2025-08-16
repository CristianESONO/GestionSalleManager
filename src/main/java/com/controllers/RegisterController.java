package com.controllers;

import com.App; // Assurez-vous que cette classe existe et a une méthode setRoot(String)
import com.core.Fabrique;
import com.entities.Role;
import com.entities.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label; // Importez Label
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.Date; // Pour la date d'inscription
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError; // Label pour afficher les messages d'erreur

    // Les champs suivants sont supprimés car ils ne sont plus dans le FXML
    // @FXML private ComboBox<String> comboRole;
    // @FXML private Button btnRegister; // onAction est directement dans le FXML
    // @FXML private Button btnBack; // onAction est directement dans le FXML

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cacher le label d'erreur au démarrage
        lblError.setVisible(false);
        // Le ComboBox de rôle n'est plus présent dans le FXML, donc pas d'initialisation ici.
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Cacher et vider le label d'erreur avant chaque tentative
        lblError.setVisible(false);
        lblError.setText("");

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // --- Validations des champs ---
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            lblError.setVisible(true);
            return;
        }
        
        // Validation d'email simple (peut être améliorée avec des regex plus robustes)
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            lblError.setText("Format d'email invalide.");
            lblError.setVisible(true);
            return;
        }

        // Création de l'utilisateur (User de base)
        User newUser = new User(name, email, password, new Date()); // Date d'inscription actuelle
        // Définir le rôle automatiquement comme SuperAdmin
        newUser.setRole(Role.SuperAdmin); 

        try {
            // Appel au service pour ajouter l'utilisateur
            Fabrique.getService().addUser(newUser);
            showAlert(AlertType.INFORMATION, "Succès", "Inscription réussie", "Le compte SuperAdmin a été créé.");
            App.setRoot("connexion"); // Revenir à la page de connexion
        } catch (Exception e) {
            // Afficher l'erreur retournée par la couche de service
            lblError.setText("Erreur d'inscription : " + e.getMessage());
            lblError.setVisible(true);
            e.printStackTrace(); // Pour le débogage
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Utilise App.setRoot pour naviguer vers la vue de connexion
        App.setRoot("connexion");
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
