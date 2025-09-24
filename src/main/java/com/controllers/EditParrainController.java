package com.controllers;

import com.entities.Parrain;
import com.core.Fabrique;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EditParrainController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    //@FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField codeParrainageField;

    private Parrain parrainToEdit; // Le parrain à modifier

    @FXML
    private void initialize() {
        // Limiter le champ téléphone à 9 chiffres
        setupPhoneField();
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



    /**
     * Charge les informations du parrain dans les champs du formulaire.
     * @param parrain Le parrain à modifier.
     */
    public void setParrainToEdit(Parrain parrain) {
        this.parrainToEdit = parrain;
        nameField.setText(parrain.getName());
        emailField.setText(parrain.getEmail());
        //passwordField.setText(parrain.getPassword());
        phoneField.setText(parrain.getPhone());
        addressField.setText(parrain.getAddress());
        codeParrainageField.setText(parrain.getCodeParrainage());
    }

    @FXML
    private void saveParrain() throws Exception {
        // Récupérer les nouvelles valeurs
        String name = nameField.getText();
        String email = emailField.getText();
        //String password = passwordField.getText();
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

        // Mettre à jour les informations du parrain
        parrainToEdit.setName(name);
        parrainToEdit.setEmail(email);
        //parrainToEdit.setPassword(password);
        parrainToEdit.setPhone(phone);
        parrainToEdit.setAddress(address);
        parrainToEdit.setCodeParrainage(codeParrainage);

        // Enregistrer les modifications via le service
        Fabrique.getService().updateParrain(parrainToEdit);

        // Afficher un message de succès
        ControllerUtils.showInfoAlert("Succès", "Les modifications ont été enregistrées avec succès.");

        // Fermer la fenêtre
        ControllerUtils.closeWindow(nameField);
    }

    @FXML
    private void cancel() {
        ControllerUtils.closeWindow(nameField);
    }
}