package com.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import com.entities.Role;
import com.entities.User;
import com.core.Fabrique;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController implements Initializable {

    @FXML
    private Button btnBack;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private ComboBox<String> comboRole;

    @FXML
    private Button btnRegister;

    @FXML
    private PasswordField txtPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Associer les actions des boutons
        btnRegister.setOnAction(event -> handleInscrire());
        btnBack.setOnAction(event -> handleRetour());

        // Charger les rôles disponibles
        comboRole.getItems().add(Role.SuperAdmin.name());
    }

    @FXML
    private void handleInscrire() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        // Création de l'utilisateur
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(Role.SuperAdmin);
        newUser.setRegistrationDate(new Date()); // Ajout de la date d'inscription
        

        try {
            // Ajout de l'utilisateur via le service
            Fabrique.getService().addUser(newUser);
            showAlert(AlertType.INFORMATION, "Succès", "Inscription réussie !");
            
            // Réinitialisation des champs après l'inscription
            txtName.clear();
            txtEmail.clear();
            txtPassword.clear();
            comboRole.setValue(null);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Échec de l'inscription : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            // Charger le fichier FXML de la connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/connexion.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène et l'afficher
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) btnBack.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
