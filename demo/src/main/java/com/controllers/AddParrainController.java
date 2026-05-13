package com.controllers;

import com.entities.Parrain;
import com.entities.Role;
import com.core.Fabrique;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Date;
import java.util.Random;

public class AddParrainController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField codeParrainageField;

    @FXML
    private void initialize() {
        // Limiter le champ téléphone à 9 chiffres
        setupPhoneField();
    }


    @FXML
    private void addParrain() throws Exception {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String codeParrainage = codeParrainageField.getText();

        // Validation des champs
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || codeParrainage.isEmpty()) {
            ControllerUtils.showErrorAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        if (!ControllerUtils.isValidEmail(email)) {
            ControllerUtils.showErrorAlert("Erreur", "L'email n'est pas valide.");
            return;
        }

        if (!ControllerUtils.isValidPhone(phone)) {
            ControllerUtils.showErrorAlert("Erreur", "Le numéro de téléphone n'est pas valide.");
            return;
        }

        // Générer un mot de passe aléatoire
        String password = generateRandomPassword();
        // Utiliser la date et l'heure actuelles
        Date registrationDate = new Date();
        int parrainagePoints = 0; // Initialiser les points de parrainage à 0

        // Créer un nouveau parrain
        Parrain newParrain = new Parrain(name, email, password, registrationDate, phone, address, codeParrainage, parrainagePoints);
        newParrain.setRole(Role.Parrain);

        // Ajouter le parrain via le service
        Fabrique.getService().addParrain(newParrain);

        // Afficher un message de succès
        ControllerUtils.showInfoAlert("Succès", "Le parrain a été ajouté avec succès.");

        // Effacer les champs et fermer la fenêtre
        clearFields();
        ControllerUtils.closeWindow(nameField);
    }

    private void setupPhoneField() {
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                return;
            }

            // Ne garder que les chiffres
            String filteredValue = newValue.replaceAll("[^0-9]", "");

            // Limiter à 9 chiffres
            if (filteredValue.length() > 9) {
                filteredValue = filteredValue.substring(0, 9);
            }

            // Mettre à jour le champ si nécessaire
            if (!filteredValue.equals(newValue)) {
                phoneField.setText(filteredValue);
            }
        });
    }


    @FXML
    private void cancel() {
        clearFields();
        ControllerUtils.closeWindow(nameField);
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        codeParrainageField.clear();
    }

    /**
     * Génère un mot de passe aléatoire.
     * @return Un mot de passe aléatoire de 10 caractères.
     */
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }
}