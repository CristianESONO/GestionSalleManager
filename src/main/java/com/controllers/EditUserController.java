package com.controllers;

import com.entities.User;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.core.Fabrique;
import com.entities.Role;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditUserController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private User selectedUser;

   public void setUser(User user) {
        selectedUser = user;
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        roleComboBox.getItems().addAll(Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toList()));
        roleComboBox.setValue(user.getRole().name());
    }

    @FXML
    private void editUser() throws Exception {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            ControllerUtils.showErrorAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        if (!ControllerUtils.isValidEmail(email)) {
            ControllerUtils.showErrorAlert("Erreur", "L'email n'est pas valide.");
            return;
        }

        Role userRole = Role.valueOf(role);
        selectedUser.setName(name);
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);
        selectedUser.setRole(userRole);

        // Sauvegarder les modifications (à implémenter dans votre service)
        // UtilisateurService.update(selectedUser);
        Fabrique.getService().updateUser(selectedUser);
        ControllerUtils.showInfoAlert("Succès", "L'utilisateur a été ajouté avec succès.");
        clearFields();

        ControllerUtils.closeWindow(nameField);
    }

    @FXML
    private void cancel() {
        clearFields();
        ControllerUtils.closeWindow(nameField);
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
}