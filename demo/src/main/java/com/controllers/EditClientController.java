package com.controllers;

import com.core.Fabrique;
import com.entities.Client;
import com.entities.Role;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class EditClientController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDateField;
    @FXML private TextField addressField;
    @FXML private TextField loyaltyPointsField;
    @FXML private ComboBox<String> roleComboBox;

    private Client selectedClient;

    public void setClient(Client client) {
        selectedClient = client;
        nameField.setText(client.getName());
        emailField.setText(client.getEmail());
        phoneField.setText(client.getPhone());
    
        // Conversion de la date de naissance en LocalDate
        if (client.getBirthDate() != null) {
            if (client.getBirthDate() instanceof java.sql.Date) {
                // Si c'est un java.sql.Date, utilisez toLocalDate()
                java.sql.Date sqlDate = (java.sql.Date) client.getBirthDate();
                birthDateField.setValue(sqlDate.toLocalDate());
            } else if (client.getBirthDate() instanceof java.util.Date) {
                // Si c'est un java.util.Date, utilisez toInstant()
                java.util.Date utilDate = (java.util.Date) client.getBirthDate();
                LocalDate localDate = utilDate.toInstant()
                                              .atZone(ZoneId.systemDefault())
                                              .toLocalDate();
                birthDateField.setValue(localDate);
            }
        } else {
            birthDateField.setValue(null); // Si la date est nulle, définissez le champ comme vide
        }
    
        addressField.setText(client.getAddress());
        loyaltyPointsField.setText(String.valueOf(client.getLoyaltyPoints()));
    
        // Remplir le ComboBox des rôles
        roleComboBox.getItems().addAll(Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toList()));
        roleComboBox.setValue(client.getRole().name());
    }

    @FXML
private void editClient() throws Exception {
    String name = nameField.getText();
    String email = emailField.getText();
    String phone = phoneField.getText();
    LocalDate birthDate = birthDateField.getValue();
    String address = addressField.getText();
    String loyaltyPointsStr = loyaltyPointsField.getText();
    String role = roleComboBox.getValue();

    // Validation des champs
    if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || birthDate == null || address.isEmpty() || loyaltyPointsStr.isEmpty() || role == null) {
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

    // Conversion de LocalDate en java.util.Date ou java.sql.Date
    Date date;
    if (selectedClient.getBirthDate() instanceof java.sql.Date) {
        date = java.sql.Date.valueOf(birthDate); // Conversion en java.sql.Date
    } else {
        date = java.util.Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()); // Conversion en java.util.Date
    }

    int loyaltyPoints = Integer.parseInt(loyaltyPointsStr);
    Role clientRole = Role.valueOf(role);

    // Mettre à jour les informations du client
    selectedClient.setName(name);
    selectedClient.setEmail(email);
    selectedClient.setPhone(phone);
    selectedClient.setBirthDate(date); // Utilisation de la date convertie
    selectedClient.setAddress(address);
    selectedClient.setLoyaltyPoints(loyaltyPoints);
    selectedClient.setRole(clientRole);

    // Sauvegarder les modifications
    Fabrique.getService().updateClient(selectedClient);
    ControllerUtils.showInfoAlert("Succès", "L'utilisateur a été modifié avec succès.");
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
        birthDateField.setValue(null);
        addressField.clear();
        loyaltyPointsField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
}