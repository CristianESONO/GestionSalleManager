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
    @FXML private TextField addressField;
    @FXML private TextField loyaltyPointsField;
    @FXML private ComboBox<String> roleComboBox;

    private Client selectedClient;

    public void setClient(Client client) {
        selectedClient = client;
        nameField.setText(client.getName());
        emailField.setText(client.getEmail());
        phoneField.setText(client.getPhone());
    
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
    String address = addressField.getText();
    String loyaltyPointsStr = loyaltyPointsField.getText();
    String role = roleComboBox.getValue();

    // Validation des champs
    if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || loyaltyPointsStr.isEmpty() || role == null) {
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
   

    int loyaltyPoints = Integer.parseInt(loyaltyPointsStr);
    Role clientRole = Role.valueOf(role);

    // Mettre à jour les informations du client
    selectedClient.setName(name);
    selectedClient.setEmail(email);
    selectedClient.setPhone(phone);
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
        addressField.clear();
        loyaltyPointsField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
}