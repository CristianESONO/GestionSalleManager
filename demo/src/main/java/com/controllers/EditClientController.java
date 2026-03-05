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
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField loyaltyPointsField;
    @FXML private ComboBox<String> roleComboBox;

    private Client selectedClient;

    public void setClient(Client client) {
        selectedClient = client;
        nameField.setText(client.getName());
        phoneField.setText(client.getPhone());
        addressField.setText(client.getAddress() != null ? client.getAddress() : "");
        loyaltyPointsField.setText(String.valueOf(client.getLoyaltyPoints()));
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll(Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toList()));
            roleComboBox.setValue(client.getRole().name());
        }
    }

    @FXML
private void editClient() throws Exception {
    String name = nameField.getText() != null ? nameField.getText().trim() : "";
    String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
    String address = addressField.getText() != null ? addressField.getText().trim() : "";

    if (name.isEmpty() || phone.isEmpty()) {
        ControllerUtils.showErrorAlert("Erreur", "Veuillez renseigner le nom et le numéro de téléphone.");
        return;
    }
    if (!ControllerUtils.isValidPhone(phone)) {
        ControllerUtils.showErrorAlert("Erreur", "Le numéro de téléphone n'est pas valide.");
        return;
    }

    selectedClient.setName(name);
    selectedClient.setPhone(phone);
    selectedClient.setAddress(address.isEmpty() ? null : address);

    try {
        Fabrique.getService().updateClient(selectedClient);
    } catch (Exception e) {
        ControllerUtils.showErrorAlert("Erreur", e.getMessage() != null ? e.getMessage() : "Impossible de modifier le client.");
        return;
    }

    ControllerUtils.showInfoAlert("Succès", "Le client a été modifié avec succès.");
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
        phoneField.clear();
        addressField.clear();
        loyaltyPointsField.clear();
        if (roleComboBox != null) roleComboBox.getSelectionModel().clearSelection();
    }
}