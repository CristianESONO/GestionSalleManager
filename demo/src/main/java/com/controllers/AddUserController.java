package com.controllers;

import com.core.AppConfig;
import com.core.Fabrique;
import com.entities.Role;
import com.entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt; // Import pour BCrypt

import java.util.Date;

public class AddUserController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private void initialize() {
        // Ajouter "Sélectionner un rôle" comme valeur par défaut
        roleComboBox.getItems().add("Sélectionner un rôle");
        // Ajouter uniquement les rôles Admin et SuperAdmin
        roleComboBox.getItems().add(Role.Admin.name());
        roleComboBox.getItems().add(Role.SuperAdmin.name());
    }

    @FXML
    private void addUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (!areFieldsValid(name, email, password, role)) {
            ControllerUtils.showErrorAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        if (!ControllerUtils.isValidEmail(email)) {
            ControllerUtils.showErrorAlert("Erreur", "L'email n'est pas valide.");
            return;
        }

        int minLen = AppConfig.getPasswordMinLength();
        if (password.length() < minLen) {
            ControllerUtils.showErrorAlert("Erreur", "Le mot de passe doit contenir au moins " + minLen + " caractères.");
            return;
        }

        try {
            Role userRole = Role.valueOf(role);
            // Hachage du mot de passe avec BCrypt
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User newUser = new User(name, email, hashedPassword, new Date());
            newUser.setRole(userRole);
            Fabrique.getService().addUser(newUser);
            ControllerUtils.showInfoAlert("Succès", "L'utilisateur a été ajouté avec succès.");
            clearFields();
            ControllerUtils.closeWindow(nameField);
        } catch (Exception e) {
            ControllerUtils.showErrorAlert("Erreur", "Impossible d'ajouter l'utilisateur : " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        clearFields();
        ControllerUtils.closeWindow(nameField);
    }

    private boolean areFieldsValid(String name, String email, String password, String role) {
        return !name.isEmpty() && !email.isEmpty() && !password.isEmpty() && role != null && !role.equals("Sélectionner un rôle");
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
}
