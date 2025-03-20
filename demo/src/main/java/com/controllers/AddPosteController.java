package com.controllers;

import com.core.Fabrique;
import com.entities.Poste;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class AddPosteController {

    @FXML
    private TextField posteNameField;

    @FXML
    private TextField posteStatusField;

    @FXML
    private CheckBox posteAvailableCheckBox;

    @FXML
    private Button btnAjouterPoste;

    @FXML
    private Button btnAnnuler;

    @FXML
    public void initialize() {
        // Générer le nom du poste automatiquement après son ajout
        // Le nom sera "Poste N°" + ID après la création du poste
    }

    @FXML
    private void addPoste(ActionEvent event) throws Exception {
        // Récupérer les informations du formulaire
        String posteStatus = posteStatusField.getText();
        boolean isAvailable = posteAvailableCheckBox.isSelected();

        // Créer et ajouter le poste via le service
        Poste newPoste = new Poste();
        newPoste.setStatus(posteStatus);
        newPoste.setAvailable(isAvailable);

        // Sauvegarder le poste dans la base de données
        Fabrique.getService().addPoste(newPoste);

        // Le nom du poste sera généré automatiquement lors de la sauvegarde (via getName)
        posteNameField.setText(newPoste.getName());

        // Fermer la fenêtre d'ajout
        closeWindow();
    }

    @FXML
    private void cancel(ActionEvent event) {
        // Fermer la fenêtre sans ajouter de poste
        closeWindow();
    }

    private void closeWindow() {
        // Fermer la fenêtre actuelle
        Stage stage = (Stage) btnAjouterPoste.getScene().getWindow();
        stage.close();
    }
}
