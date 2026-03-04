package com.controllers;

import com.App;
import com.core.AppConfig;
import com.core.Fabrique;
import com.entities.Role;
import com.entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import org.mindrot.jbcrypt.BCrypt; // Import pour BCrypt

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setVisible(false);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        lblError.setVisible(false);
        lblError.setText("");

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // Validations des champs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            lblError.setVisible(true);
            return;
        }

        // Validation d'email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            lblError.setText("Format d'email invalide.");
            lblError.setVisible(true);
            return;
        }

        int minLen = AppConfig.getPasswordMinLength();
        if (password.length() < minLen) {
            lblError.setText("Le mot de passe doit contenir au moins " + minLen + " caractères.");
            lblError.setVisible(true);
            return;
        }

        // Hachage du mot de passe avec BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Création de l'utilisateur avec le mot de passe haché
        User newUser = new User(name, email, hashedPassword, new Date());
        newUser.setRole(Role.SuperAdmin);

        try {
            Fabrique.getService().addUser(newUser);
            showAlert(AlertType.INFORMATION, "Succès", "Inscription réussie", "Le compte SuperAdmin a été créé.");
            App.setRoot("connexion");
        } catch (Exception e) {
            lblError.setText("Erreur d'inscription : " + e.getMessage());
            lblError.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        App.setRoot("connexion");
    }

    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
