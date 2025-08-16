package com.controllers;

import com.entities.Client;
import com.core.Fabrique;
import com.entities.Role;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

public class AddClientController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField loyaltyPointsField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    private void initialize() {
        roleComboBox.getItems().add(Role.Client.name());
    }

    @FXML
    private void addClient() throws Exception {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String loyaltyPointsStr = loyaltyPointsField.getText();

        // Validation des champs
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || loyaltyPointsStr.isEmpty()) {
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

        // Conversion des champs
        int loyaltyPoints = Integer.parseInt(loyaltyPointsStr);

        // Générer un mot de passe aléatoire
        String password = generateRandomPassword();

        // Créer un nouveau client
        Client newClient = new Client(name, email, password, new Date(), phone, address, loyaltyPoints);
        newClient.setRole(Role.Client);

        // Ajouter le client via le service
        Fabrique.getService().addClient(newClient);

        // Afficher un message de succès
        ControllerUtils.showInfoAlert("Succès", "Le client a été ajouté avec succès.");

        // Effacer les champs et fermer la fenêtre
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
        phoneField.clear();
        addressField.clear();
        loyaltyPointsField.clear();
        roleComboBox.getSelectionModel().clearSelection();
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